const router = require('express').Router();
const pool = require('../db/pool');
const { authenticate, requireRole } = require('../middleware/auth');
const { autoRouteTicket } = require('../services/routingService');

// POST /api/tickets - Tạo ticket hỗ trợ mới
router.post('/', authenticate, async (req, res) => {
  const { appId, kbId, title, description, priority, channel } = req.body;
  if (!appId || !title) return res.status(400).json({ error: 'Thiếu appId hoặc title' });

  try {
    const result = await pool.query(`
      INSERT INTO tickets (user_id, app_id, kb_id, title, description, priority, channel)
      VALUES ($1,$2,$3,$4,$5,$6,$7) RETURNING *
    `, [req.user.id, appId, kbId || null, title, description || null,
        priority || 'medium', channel || 'chat']);

    const ticket = result.rows[0];

    // Tự động điều phối KTV
    const routing = await autoRouteTicket(ticket.id, appId);

    // Thêm tin nhắn hệ thống
    await pool.query(`
      INSERT INTO chat_messages (ticket_id, sender_id, sender_role, content, msg_type)
      VALUES ($1, $2, 'system', $3, 'system')
    `, [ticket.id, req.user.id,
        routing.assigned
          ? `Ticket đã được gán cho kỹ thuật viên ${routing.technicianName}. Vui lòng chờ trong giây lát.`
          : 'Ticket đã được tạo. Hiện tại không có KTV rảnh, bạn sẽ được hỗ trợ sớm nhất có thể.'
    ]);

    res.status(201).json({ ticket, routing });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/tickets - Lấy danh sách ticket
router.get('/', authenticate, async (req, res) => {
  const { status, page = 1, limit = 20 } = req.query;
  const offset = (page - 1) * limit;

  try {
    let whereClause = '';
    const params = [];

    if (req.user.role === 'customer') {
      whereClause = 'WHERE t.user_id = $1';
      params.push(req.user.id);
    } else if (req.user.role === 'technician') {
      // KTV chỉ thấy ticket của mình
      const techResult = await pool.query('SELECT id FROM technicians WHERE user_id = $1', [req.user.id]);
      if (techResult.rows[0]) {
        whereClause = 'WHERE t.technician_id = $1';
        params.push(techResult.rows[0].id);
      }
    }

    if (status) {
      whereClause += (whereClause ? ' AND ' : 'WHERE ') + `t.status = $${params.length + 1}`;
      params.push(status);
    }

    const result = await pool.query(`
      SELECT t.*, a.name AS app_name, a.color AS app_color,
        u.full_name AS customer_name, u.email AS customer_email,
        tu.full_name AS technician_name
      FROM tickets t
      JOIN apps a ON a.id = t.app_id
      LEFT JOIN users u ON u.id = t.user_id
      LEFT JOIN technicians tech ON tech.id = t.technician_id
      LEFT JOIN users tu ON tu.id = tech.user_id
      ${whereClause}
      ORDER BY t.created_at DESC
      LIMIT $${params.length + 1} OFFSET $${params.length + 2}
    `, [...params, limit, offset]);

    res.json({ data: result.rows, page: parseInt(page) });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/tickets/:id - Chi tiết ticket + messages
router.get('/:id', authenticate, async (req, res) => {
  try {
    const [ticketResult, messagesResult] = await Promise.all([
      pool.query(`
        SELECT t.*, a.name AS app_name, a.color AS app_color,
          u.full_name AS customer_name, u.phone AS customer_phone, u.email AS customer_email,
          tu.full_name AS technician_name
        FROM tickets t
        JOIN apps a ON a.id = t.app_id
        LEFT JOIN users u ON u.id = t.user_id
        LEFT JOIN technicians tech ON tech.id = t.technician_id
        LEFT JOIN users tu ON tu.id = tech.user_id
        WHERE t.id = $1
      `, [req.params.id]),
      pool.query(`
        SELECT m.*, u.full_name AS sender_name
        FROM chat_messages m
        LEFT JOIN users u ON u.id = m.sender_id
        WHERE m.ticket_id = $1 ORDER BY m.created_at ASC
      `, [req.params.id])
    ]);

    if (!ticketResult.rows[0]) return res.status(404).json({ error: 'Không tìm thấy ticket' });
    res.json({ ticket: ticketResult.rows[0], messages: messagesResult.rows });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// PATCH /api/tickets/:id/status - Cập nhật trạng thái
router.patch('/:id/status', authenticate, async (req, res) => {
  const { status } = req.body;
  const validStatuses = ['pending','assigned','in_progress','resolved','closed'];
  if (!validStatuses.includes(status)) return res.status(400).json({ error: 'Trạng thái không hợp lệ' });

  try {
    const extra