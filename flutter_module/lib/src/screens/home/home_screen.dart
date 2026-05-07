import 'package:flutter/material.dart';
import '../../theme/app_theme.dart';
import '../../channels/app_channel.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  late AnimationController _statsController;

  final _stats = [
    _StatData(label: 'Tổng Ticket', value: 47, icon: Icons.confirmation_number_outlined, color: AppTheme.primaryBlue),
    _StatData(label: 'Đang xử lý', value: 12, icon: Icons.pending_actions_rounded, color: AppTheme.warningAmber),
    _StatData(label: 'Hoàn thành', value: 31, icon: Icons.check_circle_outline_rounded, color: AppTheme.successGreen),
    _StatData(label: 'Đánh giá XB', value: 4, icon: Icons.warning_amber_rounded, color: AppTheme.errorRed),
  ];

  final _recentTickets = [
    _TicketData(id: 'TK-1042', title: 'Máy lạnh không lạnh', customer: 'Nguyễn Văn A', status: 'Đang xử lý', priority: 'Cao', time: '10 phút trước'),
    _TicketData(id: 'TK-1041', title: 'Chảy nước cục nóng', customer: 'Trần Thị B', status: 'Mở', priority: 'Trung bình', time: '45 phút trước'),
    _TicketData(id: 'TK-1040', title: 'Bảo dưỡng định kỳ', customer: 'Lê Văn C', status: 'Đã đóng', priority: 'Thấp', time: '2 giờ trước'),
  ];

  @override
  void initState() {
    super.initState();
    _statsController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    )..forward();
  }

  @override
  void dispose() {
    _statsController.dispose();
    super.dispose();
  }

  Future<void> _onRefresh() async {
    // RefreshIndicator tự động hiển thị loading animation
    await Future.delayed(const Duration(seconds: 1));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: const Color(0xFFF5F6FA),
      body: RefreshIndicator(
        onRefresh: _onRefresh,
        color: AppTheme.primaryBlue,
        child: CustomScrollView(
          slivers: [
            _buildSliverAppBar(theme),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildStatsGrid(theme),
                    const SizedBox(height: 24),
                    _buildQuickActions(theme),
                    const SizedBox(height: 24),
                    _buildRecentTickets(theme),
                    const SizedBox(height: 80),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
      floatingActionButton: _buildFab(theme),
    );
  }

  Widget _buildSliverAppBar(ThemeData theme) {
    return SliverAppBar(
      expandedHeight: 160,
      floating: false,
      pinned: true,
      backgroundColor: AppTheme.primaryBlue,
      leading: IconButton(
        icon: const Icon(Icons.arrow_back_ios_new_rounded, color: Colors.white),
        onPressed: AppChannel.closeFlutter,
      ),
      flexibleSpace: FlexibleSpaceBar(
        background: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [AppTheme.primaryBlueDark, AppTheme.primaryBlueLight],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: SafeArea(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 60, 16, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  const Text(
                    'Chào buổi sáng! 👋',
                    style: TextStyle(
                      color: Colors.white70,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Dashboard KTV',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 26,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
      actions: [
        IconButton(
          icon: const Icon(Icons.notifications_outlined, color: Colors.white),
          onPressed: () {},
        ),
      ],
    );
  }

  Widget _buildStatsGrid(ThemeData theme) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 1.6,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
      ),
      itemCount: _stats.length,
      itemBuilder: (ctx, i) {
        return AnimatedBuilder(
          animation: _statsController,
          builder: (_, __) {
            final delay = i * 0.15;
            final progress = Curves.easeOutBack.transform(
              (((_statsController.value - delay) / (1 - delay)).clamp(0.0, 1.0)),
            );
            return Transform.scale(
              scale: progress,
              child: Opacity(
                opacity: progress.clamp(0.0, 1.0),
                child: _StatCard(data: _stats[i]),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildQuickActions(ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Thao tác nhanh',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onSurface,
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            _QuickAction(
              icon: Icons.chat_bubble_outline_rounded,
              label: 'Chat',
              color: AppTheme.primaryBlue,
              onTap: () => AppChannel.navigateToAndroid('chat'),
            ),
            const SizedBox(width: 10),
            _QuickAction(
              icon: Icons.add_circle_outline_rounded,
              label: 'Tạo Ticket',
              color: AppTheme.accentTeal,
              onTap: () => AppChannel.navigateToAndroid('createTicket'),
            ),
            const SizedBox(width: 10),
            _QuickAction(
              icon: Icons.phone_outlined,
              label: 'Gọi điện',
              color: AppTheme.warningAmber,
              onTap: () => AppChannel.navigateToAndroid('voiceCall'),
            ),
            const SizedBox(width: 10),
            _QuickAction(
              icon: Icons.bar_chart_rounded,
              label: 'Thống kê',
              color: Colors.purple,
              onTap: () => AppChannel.navigateToAndroid('statistics'),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildRecentTickets(ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Ticket gần đây',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            TextButton(
              onPressed: () => AppChannel.navigateToAndroid('tickets'),
              child: const Text('Xem tất cả'),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ..._recentTickets.map((t) => _TicketCard(data: t)),
      ],
    );
  }

  Widget _buildFab(ThemeData theme) {
    return FloatingActionButton.extended(
      onPressed: () => AppChannel.navigateToAndroid('createTicket'),
      backgroundColor: AppTheme.primaryBlue,
      foregroundColor: Colors.white,
      icon: const Icon(Icons.add_rounded),
      label: const Text('Ticket mới', style: TextStyle(fontWeight: FontWeight.w600)),
      elevation: 4,
    );
  }
}

class _StatData {
  final String label;
  final int value;
  final IconData icon;
  final Color color;
  const _StatData({required this.label, required this.value, required this.icon, required this.color});
}

class _StatCard extends StatelessWidget {
  final _StatData data;
  const _StatCard({required this.data});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: data.color.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  color: data.color.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(data.icon, color: data.color, size: 20),
              ),
              Icon(Icons.trending_up_rounded, color: AppTheme.successGreen, size: 16),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${data.value}',
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.bold,
                  color: data.color,
                ),
              ),
              Text(
                data.label,
                style: const TextStyle(
                  fontSize: 11,
                  color: Colors.grey,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _TicketData {
  final String id, title, customer, status, priority, time;
  const _TicketData({
    required this.id,
    required this.title,
    required this.customer,
    required this.status,
    required this.priority,
    required this.time,
  });
}

class _TicketCard extends StatelessWidget {
  final _TicketData data;
  const _TicketCard({required this.data});

  @override
  Widget build(BuildContext context) {
    final statusColor = AppTheme.statusColor(data.status);
    final priorityColor = AppTheme.priorityColor(data.priority);

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 4,
            height: 48,
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
                      data.id,
                      style: TextStyle(
                        fontSize: 11,
                        color: AppTheme.primaryBlue,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                      decoration: BoxDecoration(
                        color: statusColor.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        data.status,
                        style: TextStyle(
                          fontSize: 10,
                          color: statusColor,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 3),
                Text(
                  data.title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  '${data.customer} • ${data.time}',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right_rounded, color: Colors.grey),
        ],
      ),
    );
  }
}

class _QuickAction extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _QuickAction({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: color.withOpacity(0.08),
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: color.withOpacity(0.15)),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, color: color, size: 24),
              const SizedBox(height: 4),
              Text(
                label,
                style: TextStyle(
                  fontSize: 10,
                  color: color,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
