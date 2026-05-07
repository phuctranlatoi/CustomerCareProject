import 'package:flutter/material.dart';
import '../../theme/app_theme.dart';
import '../../channels/app_channel.dart';

class TicketListScreen extends StatefulWidget {
  const TicketListScreen({super.key});

  @override
  State<TicketListScreen> createState() => _TicketListScreenState();
}

class _TicketListScreenState extends State<TicketListScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  String _searchQuery = '';
  final TextEditingController _searchController = TextEditingController();

  final _allTickets = [
    _Ticket(id: 'TK-1042', title: 'Máy lạnh không lạnh', customer: 'Nguyễn Văn A', status: 'Đang xử lý', priority: 'Cao', product: 'Daikin 1.5HP', time: '10 phút trước', hasNote: true),
    _Ticket(id: 'TK-1041', title: 'Chảy nước cục nóng', customer: 'Trần Thị B', status: 'Mở', priority: 'Trung bình', product: 'Panasonic 2HP', time: '45 phút trước', hasFollowUp: true),
    _Ticket(id: 'TK-1040', title: 'Bảo dưỡng định kỳ 6 tháng', customer: 'Lê Văn C', status: 'Đã đóng', priority: 'Thấp', product: 'Toshiba 1HP', time: '2 giờ trước'),
    _Ticket(id: 'TK-1039', title: 'Máy bơm nước không hoạt động', customer: 'Phạm Thị D', status: 'Chờ phản hồi', priority: 'Cao', product: 'Panasonic 1HP', time: '3 giờ trước', isOverdue: true),
    _Ticket(id: 'TK-1038', title: 'Remote không điều khiển được', customer: 'Hoàng Văn E', status: 'Đang xử lý', priority: 'Thấp', product: 'Mitsubishi 2HP', time: '5 giờ trước'),
    _Ticket(id: 'TK-1037', title: 'Tiếng ồn lớn từ cục nóng', customer: 'Vũ Thị F', status: 'Mở', priority: 'Trung bình', product: 'Daikin 2HP', time: 'Hôm qua'),
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  List<_Ticket> get _filteredTickets {
    var list = _allTickets;
    if (_searchQuery.isNotEmpty) {
      list = list
          .where((t) =>
              t.title.toLowerCase().contains(_searchQuery.toLowerCase()) ||
              t.customer.toLowerCase().contains(_searchQuery.toLowerCase()) ||
              t.id.toLowerCase().contains(_searchQuery.toLowerCase()))
          .toList();
    }
    final tab = _tabController.index;
    if (tab == 1) list = list.where((t) => t.status == 'Mở' || t.status == 'Đang xử lý').toList();
    if (tab == 2) list = list.where((t) => t.status == 'Chờ phản hồi').toList();
    if (tab == 3) list = list.where((t) => t.status == 'Đã đóng').toList();
    return list;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F6FA),
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded),
          onPressed: () {
            if (Navigator.canPop(context)) {
              Navigator.pop(context);
            } else {
              AppChannel.closeFlutter();
            }
          },
        ),
        title: const Text('Danh sách Ticket'),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(90),
          child: Column(
            children: [
              // Search bar
              Padding(
                padding: const EdgeInsets.fromLTRB(12, 0, 12, 8),
                child: TextField(
                  controller: _searchController,
                  onChanged: (v) => setState(() => _searchQuery = v),
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Tìm kiếm ticket...',
                    hintStyle: const TextStyle(color: Colors.white60),
                    prefixIcon: const Icon(Icons.search_rounded, color: Colors.white60),
                    filled: true,
                    fillColor: Colors.white.withOpacity(0.15),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                  ),
                ),
              ),
              // Tab bar
              TabBar(
                controller: _tabController,
                onTap: (_) => setState(() {}),
                isScrollable: true,
                tabAlignment: TabAlignment.start,
                indicatorColor: Colors.white,
                labelColor: Colors.white,
                unselectedLabelColor: Colors.white60,
                tabs: const [
                  Tab(text: 'Tất cả'),
                  Tab(text: 'Đang xử lý'),
                  Tab(text: 'Chờ phản hồi'),
                  Tab(text: 'Đã đóng'),
                ],
              ),
            ],
          ),
        ),
      ),
      body: _filteredTickets.isEmpty
          ? _buildEmpty()
          : ListView.builder(
              padding: const EdgeInsets.all(12),
              itemCount: _filteredTickets.length,
              itemBuilder: (ctx, i) {
                return _TicketCard(
                  ticket: _filteredTickets[i],
                  onTap: () => AppChannel.openTicketDetail(_filteredTickets[i].id),
                );
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => AppChannel.navigateToAndroid('createTicket'),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        child: const Icon(Icons.add_rounded),
      ),
    );
  }

  Widget _buildEmpty() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.inbox_outlined,
            size: 72,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            'Không có ticket nào',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}

class _Ticket {
  final String id, title, customer, status, priority, product, time;
  final bool hasNote, hasFollowUp, isOverdue;

  const _Ticket({
    required this.id,
    required this.title,
    required this.customer,
    required this.status,
    required this.priority,
    required this.product,
    required this.time,
    this.hasNote = false,
    this.hasFollowUp = false,
    this.isOverdue = false,
  });
}

class _TicketCard extends StatelessWidget {
  final _Ticket ticket;
  final VoidCallback onTap;

  const _TicketCard({required this.ticket, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final statusColor = AppTheme.statusColor(ticket.status);
    final priorityColor = AppTheme.priorityColor(ticket.priority);

    return Dismissible(
      key: Key(ticket.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        margin: const EdgeInsets.only(bottom: 10),
        decoration: BoxDecoration(
          color: AppTheme.errorRed,
          borderRadius: BorderRadius.circular(16),
        ),
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.close_rounded, color: Colors.white, size: 28),
            Text('Đóng', style: TextStyle(color: Colors.white, fontSize: 11)),
          ],
        ),
      ),
      confirmDismiss: (_) async {
        return await showDialog<bool>(
          context: context,
          builder: (_) => AlertDialog(
            title: const Text('Xác nhận đóng ticket'),
            content: Text('Đóng ticket "${ticket.title}"?'),
            actions: [
              TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Huỷ')),
              FilledButton(onPressed: () => Navigator.pop(context, true), child: const Text('Đóng')),
            ],
          ),
        ) ?? false;
      },
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          margin: const EdgeInsets.only(bottom: 10),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.05),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(14),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Priority bar
                    Container(
                      width: 4,
                      height: 60,
                      decoration: BoxDecoration(
                        color: priorityColor,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                ticket.id,
                                style: TextStyle(
                                  fontSize: 11,
                                  color: AppTheme.primaryBlue,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              const SizedBox(width: 8),
                              _StatusBadge(status: ticket.status, color: statusColor),
                              if (ticket.isOverdue) ...[
                                const SizedBox(width: 6),
                                _OverdueBadge(),
                              ],
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text(
                            ticket.title,
                            style: const TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            ticket.customer,
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[600],
                            ),
                          ),
                          const SizedBox(height: 2),
                          Row(
                            children: [
                              Icon(Icons.devices_rounded, size: 12, color: AppTheme.primaryBlue),
                              const SizedBox(width: 4),
                              Text(
                                ticket.product,
                                style: TextStyle(
                                  fontSize: 11,
                                  color: AppTheme.primaryBlue,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          ticket.time,
                          style: TextStyle(fontSize: 11, color: Colors.grey[500]),
                        ),
                        const SizedBox(height: 8),
                        const Icon(Icons.chevron_right_rounded, color: Colors.grey),
                      ],
                    ),
                  ],
                ),
              ),
              // Badges row
              if (ticket.hasNote || ticket.hasFollowUp)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.grey[50],
                    borderRadius: const BorderRadius.vertical(bottom: Radius.circular(16)),
                  ),
                  child: Row(
                    children: [
                      if (ticket.hasNote)
                        _Badge(
                          icon: '📝',
                          label: 'Có ghi chú',
                          color: AppTheme.primaryBlue,
                        ),
                      if (ticket.hasNote && ticket.hasFollowUp)
                        const SizedBox(width: 8),
                      if (ticket.hasFollowUp)
                        _Badge(
                          icon: '🔴',
                          label: 'Follow-up đánh giá xấu',
                          color: AppTheme.errorRed,
                        ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final String status;
  final Color color;

  const _StatusBadge({required this.status, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        status,
        style: TextStyle(
          fontSize: 10,
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _OverdueBadge extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
      decoration: BoxDecoration(
        color: AppTheme.errorRed.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: AppTheme.errorRed.withOpacity(0.3)),
      ),
      child: Text(
        '⚠ Quá hạn',
        style: TextStyle(
          fontSize: 10,
          color: AppTheme.errorRed,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  final String icon, label;
  final Color color;

  const _Badge({required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(icon, style: const TextStyle(fontSize: 11)),
        const SizedBox(width: 4),
        Text(
          label,
          style: TextStyle(
            fontSize: 11,
            color: color,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}
