import 'package:flutter/material.dart';
import '../../theme/app_theme.dart';
import 'chat_screen.dart';

class ChatBubble extends StatelessWidget {
  final ChatMessage message;
  final String timeString;
  final bool isSequential;

  const ChatBubble({
    super.key,
    required this.message,
    required this.timeString,
    this.isSequential = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isKtv = message.isFromKtv;

    return Padding(
      padding: EdgeInsets.only(
        top: isSequential ? 2 : 8,
        bottom: 2,
      ),
      child: Row(
        mainAxisAlignment: isKtv ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isKtv) ...[
            if (!isSequential)
              _Avatar(name: message.senderName ?? 'K', size: 28)
            else
              const SizedBox(width: 28),
            const SizedBox(width: 6),
          ],
          Flexible(
            child: Column(
              crossAxisAlignment:
                  isKtv ? CrossAxisAlignment.end : CrossAxisAlignment.start,
              children: [
                if (!isKtv && !isSequential && message.senderName != null)
                  Padding(
                    padding: const EdgeInsets.only(left: 4, bottom: 2),
                    child: Text(
                      message.senderName!,
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  ),
                Container(
                  constraints: BoxConstraints(
                    maxWidth: MediaQuery.of(context).size.width * 0.72,
                  ),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 14,
                    vertical: 10,
                  ),
                  decoration: BoxDecoration(
                    gradient: isKtv
                        ? const LinearGradient(
                            colors: [AppTheme.primaryBlueDark, AppTheme.primaryBlue],
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                          )
                        : null,
                    color: isKtv ? null : theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.only(
                      topLeft: const Radius.circular(18),
                      topRight: const Radius.circular(18),
                      bottomLeft: Radius.circular(isKtv ? 18 : (isSequential ? 18 : 4)),
                      bottomRight: Radius.circular(isKtv ? (isSequential ? 18 : 4) : 18),
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: isKtv
                            ? AppTheme.primaryBlue.withOpacity(0.25)
                            : Colors.black.withOpacity(0.05),
                        blurRadius: 6,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        message.content,
                        style: TextStyle(
                          fontSize: 14.5,
                          height: 1.4,
                          color: isKtv
                              ? Colors.white
                              : theme.colorScheme.onSurface,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        timeString,
                        style: TextStyle(
                          fontSize: 10,
                          color: isKtv
                              ? Colors.white54
                              : theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          if (isKtv) const SizedBox(width: 4),
        ],
      ),
    );
  }
}

class _Avatar extends StatelessWidget {
  final String name;
  final double size;

  const _Avatar({required this.name, this.size = 32});

  @override
  Widget build(BuildContext context) {
    return CircleAvatar(
      radius: size / 2,
      backgroundColor: AppTheme.primaryBlue.withOpacity(0.12),
      child: Text(
        name[0].toUpperCase(),
        style: TextStyle(
          color: AppTheme.primaryBlue,
          fontWeight: FontWeight.bold,
          fontSize: size * 0.42,
        ),
      ),
    );
  }
}
