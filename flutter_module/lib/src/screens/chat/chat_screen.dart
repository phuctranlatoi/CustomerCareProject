import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../channels/app_channel.dart';
import '../../theme/app_theme.dart';
import 'chat_bubble.dart';
import 'typing_indicator.dart';

class ChatMessage {
  final String id;
  final String content;
  final bool isFromKtv;
  final DateTime timestamp;
  final String? senderName;
  final MessageType type;

  const ChatMessage({
    required this.id,
    required this.content,
    required this.isFromKtv,
    required this.timestamp,
    this.senderName,
    this.type = MessageType.text,
  });
}

enum MessageType { text, image, system }

class ChatScreen extends StatefulWidget {
  final String? chatId;
  final String? customerName;

  const ChatScreen({super.key, this.chatId, this.customerName});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> with TickerProviderStateMixin {
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final FocusNode _focusNode = FocusNode();
  bool _isTyping = false;
  bool _showTypingIndicator = false;
  late AnimationController _sendButtonController;

  // Dữ liệu demo — trong thực tế nhận từ Android qua MethodChannel
  final List<ChatMessage> _messages = [
    ChatMessage(
      id: '1',
      content: 'Xin chào! Tôi cần hỗ trợ về sản phẩm máy lạnh Daikin 1.5HP.',
      isFromKtv: false,
      senderName: 'Nguyễn Văn A',
      timestamp: DateTime.now().subtract(const Duration(minutes: 25)),
    ),
    ChatMessage(
      id: '2',
      content: 'Chào anh/chị! Tôi là kỹ thuật viên phụ trách. Anh/chị mô tả vấn đề cụ thể nhé?',
      isFromKtv: true,
      timestamp: DateTime.now().subtract(const Duration(minutes: 24)),
    ),
    ChatMessage(
      id: '3',
      content: 'Máy bị chảy nước, không làm lạnh được. Đã xảy ra từ sáng nay.',
      isFromKtv: false,
      senderName: 'Nguyễn Văn A',
      timestamp: DateTime.now().subtract(const Duration(minutes: 20)),
    ),
    ChatMessage(
      id: '4',
      content: 'Anh/chị kiểm tra xem đèn báo lỗi trên máy hiện mã gì không? Thường là dãy đèn nháy.',
      isFromKtv: true,
      timestamp: DateTime.now().subtract(const Duration(minutes: 18)),
    ),
    ChatMessage(
      id: '5',
      content: 'Tôi thấy đèn timer nháy 5 lần liên tục.',
      isFromKtv: false,
      senderName: 'Nguyễn Văn A',
      timestamp: DateTime.now().subtract(const Duration(minutes: 15)),
    ),
    ChatMessage(
      id: '6',
      content: '5 lần nháy là lỗi cảm biến nhiệt độ phòng. Tôi sẽ tạo ticket và cử kỹ thuật viên đến kiểm tra trong hôm nay. Anh/chị cho biết địa chỉ cụ thể nhé?',
      isFromKtv: true,
      timestamp: DateTime.now().subtract(const Duration(minutes: 12)),
    ),
  ];

  @override
  void initState() {
    super.initState();
    _sendButtonController = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );

    // Nhận data từ Android nếu có
    AppChannel.setMethodCallHandler((call) async {
      if (call.method == 'newMessage' && mounted) {
        final data = call.arguments as Map;
        setState(() {
          _messages.add(ChatMessage(
            id: DateTime.now().millisecondsSinceEpoch.toString(),
            content: data['content'] as String,
            isFromKtv: false,
            senderName: data['senderName'] as String?,
            timestamp: DateTime.now(),
          ));
        });
        _scrollToBottom();
      }
    });

    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());
  }

  @override
  void dispose() {
    _textController.dispose();
    _scrollController.dispose();
    _focusNode.dispose();
    _sendButtonController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  void _sendMessage() {
    final text = _textController.text.trim();
    if (text.isEmpty) return;

    HapticFeedback.lightImpact();

    setState(() {
      _messages.add(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        content: text,
        isFromKtv: true,
        timestamp: DateTime.now(),
      ));
      _isTyping = false;
    });

    _textController.clear();
    _sendButtonController.reverse();
    _scrollToBottom();

    // Giả lập typing indicator từ khách hàng (demo)
    Future.delayed(const Duration(milliseconds: 800), () {
      if (mounted) {
        setState(() => _showTypingIndicator = true);
        _scrollToBottom();
      }
    });
    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) setState(() => _showTypingIndicator = false);
    });
  }

  String _formatTime(DateTime dt) {
    final h = dt.hour.toString().padLeft(2, '0');
    final m = dt.minute.toString().padLeft(2, '0');
    return '$h:$m';
  }

  bool _showDateSeparator(int index) {
    if (index == 0) return true;
    final prev = _messages[index - 1].timestamp;
    final curr = _messages[index].timestamp;
    return prev.day != curr.day;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final customerName = widget.customerName ?? 'Nguyễn Văn A';

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: _buildAppBar(theme, customerName),
      body: Column(
        children: [
          Expanded(
            child: _buildMessageList(theme),
          ),
          if (_showTypingIndicator) const TypingIndicator(),
          _buildInputArea(theme),
        ],
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(ThemeData theme, String customerName) {
    return AppBar(
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
      title: Row(
        children: [
          Hero(
            tag: 'avatar_$customerName',
            child: CircleAvatar(
              radius: 18,
              backgroundColor: AppTheme.primaryBlue.withOpacity(0.15),
              child: Text(
                customerName.isNotEmpty ? customerName[0].toUpperCase() : 'K',
                style: TextStyle(
                  color: AppTheme.primaryBlue,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                customerName,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: Colors.white,
                ),
              ),
              const Text(
                'Đang hoạt động',
                style: TextStyle(
                  fontSize: 11,
                  color: Colors.white70,
                ),
              ),
            ],
          ),
        ],
      ),
      actions: [
        IconButton(
          icon: const Icon(Icons.phone_rounded),
          onPressed: () => AppChannel.navigateToAndroid('voiceCall'),
          tooltip: 'Gọi điện',
        ),
        IconButton(
          icon: const Icon(Icons.more_vert_rounded),
          onPressed: _showMoreOptions,
        ),
      ],
    );
  }

  Widget _buildMessageList(ThemeData theme) {
    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      itemCount: _messages.length,
      itemBuilder: (context, index) {
        final msg = _messages[index];
        return Column(
          children: [
            if (_showDateSeparator(index))
              _buildDateSeparator(msg.timestamp, theme),
            ChatBubble(
              message: msg,
              timeString: _formatTime(msg.timestamp),
              isSequential: index > 0 &&
                  _messages[index - 1].isFromKtv == msg.isFromKtv &&
                  msg.timestamp.difference(_messages[index - 1].timestamp).inMinutes < 2,
            ),
          ],
        );
      },
    );
  }

  Widget _buildDateSeparator(DateTime date, ThemeData theme) {
    final now = DateTime.now();
    String label;
    if (date.day == now.day) {
      label = 'Hôm nay';
    } else if (date.day == now.day - 1) {
      label = 'Hôm qua';
    } else {
      label = '${date.day}/${date.month}/${date.year}';
    }

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Row(
        children: [
          Expanded(child: Divider(color: theme.colorScheme.outline.withOpacity(0.3))),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                label,
                style: TextStyle(
                  fontSize: 11,
                  color: theme.colorScheme.onSurfaceVariant,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),
          Expanded(child: Divider(color: theme.colorScheme.outline.withOpacity(0.3))),
        ],
      ),
    );
  }

  Widget _buildInputArea(ThemeData theme) {
    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        border: Border(
          top: BorderSide(
            color: theme.colorScheme.outline.withOpacity(0.15),
          ),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      padding: EdgeInsets.only(
        left: 8,
        right: 8,
        top: 8,
        bottom: MediaQuery.of(context).viewInsets.bottom > 0
            ? 8
            : MediaQuery.of(context).padding.bottom + 8,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          IconButton(
            icon: Icon(
              Icons.add_circle_outline_rounded,
              color: theme.colorScheme.primary,
            ),
            onPressed: _showAttachmentOptions,
          ),
          Expanded(
            child: Container(
              constraints: const BoxConstraints(maxHeight: 120),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(24),
              ),
              child: TextField(
                controller: _textController,
                focusNode: _focusNode,
                maxLines: null,
                keyboardType: TextInputType.multiline,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(
                  hintText: 'Nhập tin nhắn...',
                  border: InputBorder.none,
                  contentPadding: EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 10,
                  ),
                ),
                onChanged: (val) {
                  final typing = val.trim().isNotEmpty;
                  if (typing != _isTyping) {
                    setState(() => _isTyping = typing);
                    if (typing) {
                      _sendButtonController.forward();
                    } else {
                      _sendButtonController.reverse();
                    }
                  }
                },
              ),
            ),
          ),
          const SizedBox(width: 4),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 200),
            transitionBuilder: (child, animation) => ScaleTransition(
              scale: animation,
              child: child,
            ),
            child: _isTyping
                ? _SendButton(key: const ValueKey('send'), onTap: _sendMessage)
                : _MicButton(key: const ValueKey('mic')),
          ),
        ],
      ),
    );
  }

  void _showMoreOptions() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _ChatMoreOptionsSheet(),
    );
  }

  void _showAttachmentOptions() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _AttachmentSheet(),
    );
  }
}

class _SendButton extends StatelessWidget {
  final VoidCallback onTap;
  const _SendButton({super.key, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [AppTheme.primaryBlue, AppTheme.primaryBlueLight],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: AppTheme.primaryBlue.withOpacity(0.35),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: const Icon(Icons.send_rounded, color: Colors.white, size: 20),
      ),
    );
  }
}

class _MicButton extends StatelessWidget {
  const _MicButton({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        shape: BoxShape.circle,
      ),
      child: Icon(
        Icons.mic_none_rounded,
        color: Theme.of(context).colorScheme.onSurfaceVariant,
        size: 22,
      ),
    );
  }
}

class _ChatMoreOptionsSheet extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 4,
            margin: const EdgeInsets.only(bottom: 20),
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          _OptionTile(
            icon: Icons.confirmation_number_outlined,
            label: 'Tạo ticket hỗ trợ',
            color: AppTheme.primaryBlue,
            onTap: () {
              Navigator.pop(context);
              AppChannel.navigateToAndroid('createTicket');
            },
          ),
          _OptionTile(
            icon: Icons.star_outline_rounded,
            label: 'Yêu cầu đánh giá',
            color: AppTheme.warningAmber,
            onTap: () => Navigator.pop(context),
          ),
          _OptionTile(
            icon: Icons.history_rounded,
            label: 'Lịch sử chat',
            color: AppTheme.accentTeal,
            onTap: () => Navigator.pop(context),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }
}

class _AttachmentSheet extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 4,
            margin: const EdgeInsets.only(bottom: 20),
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _AttachIcon(icon: Icons.photo_library_rounded, label: 'Thư viện', color: Colors.purple),
              _AttachIcon(icon: Icons.camera_alt_rounded, label: 'Camera', color: Colors.blue),
              _AttachIcon(icon: Icons.insert_drive_file_rounded, label: 'Tài liệu', color: Colors.orange),
              _AttachIcon(icon: Icons.location_on_rounded, label: 'Vị trí', color: Colors.green),
            ],
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}

class _AttachIcon extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;

  const _AttachIcon({required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: color, size: 26),
        ),
        const SizedBox(height: 6),
        Text(label, style: const TextStyle(fontSize: 11)),
      ],
    );
  }
}

class _OptionTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _OptionTile({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(icon, color: color, size: 22),
      ),
      title: Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
      trailing: const Icon(Icons.chevron_right_rounded, color: Colors.grey),
      onTap: onTap,
    );
  }
}
