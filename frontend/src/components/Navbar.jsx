import { useState, useEffect, useRef } from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Badge, Menu, MenuItem, Box, Avatar, Tooltip, Divider } from '@mui/material';
import { Notifications as NotificationsIcon, Logout as LogoutIcon, MarkEmailRead as ReadIcon, WarningAmber as AlertIcon } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import * as notificationService from '../services/notificationService';
import { SimpleStompClient } from '../utils/stompClient';

export default function Navbar({ onTabChange, activeTab }) {
  const { user, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [anchorElNotifications, setAnchorElNotifications] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const socketRef = useRef(null);

  useEffect(() => {
    if (user) {
      loadNotifications();

      // Establish WebSocket subscription for notifications
      const wsUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api') + '/ws';
      const client = new SimpleStompClient(wsUrl, () => {
        client.subscribe(`/user/${user.email}/queue/notifications`);
        client.subscribe('/topic/listings'); // public listings channel
      }, (msg) => {
        // Notification payload received in real-time
        setNotifications(prev => [msg, ...prev]);
        if (!msg.readAt) {
          setUnreadCount(c => c + 1);
        }
      });
      client.connect();
      socketRef.current = client;

      return () => {
        if (socketRef.current) {
          socketRef.current.disconnect();
        }
      };
    }
  }, [user]);

  async function loadNotifications() {
    try {
      const res = await notificationService.getMyNotifications(0, 10);
      setNotifications(res.content || []);
      // unread counts
      const unread = (res.content || []).filter(n => !n.readAt).length;
      setUnreadCount(unread);
    } catch (e) {
      console.warn("Failed to load notifications: ", e);
    }
  }

  async function handleMarkAllAsRead() {
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, readAt: new Date().toISOString() })));
      setUnreadCount(0);
    } catch (e) {
      console.error(e);
    }
  }

  async function handleMarkAsRead(id) {
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, readAt: new Date().toISOString() } : n));
      setUnreadCount(c => Math.max(0, c - 1));
    } catch (e) {
      console.error(e);
    }
  }

  return (
    <AppBar position="sticky" sx={{ backdropFilter: 'blur(8px)', backgroundColor: 'rgba(24, 29, 28, 0.85)', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 'bold', letterSpacing: 0.5, color: '#f2b84b', cursor: 'pointer' }} onClick={() => onTabChange('home')}>
          ♻️ Surplus Food Marketplace
        </Typography>

        {user && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)' }}>
              Logged in as: <strong>{user.fullName}</strong> ({user.roles[0]?.substring(5).replace('_', ' ')})
            </Typography>

            {/* Notification Bell */}
            <IconButton color="inherit" onClick={(e) => setAnchorElNotifications(e.currentTarget)}>
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>

            <Menu
              anchorEl={anchorElNotifications}
              open={Boolean(anchorElNotifications)}
              onClose={() => setAnchorElNotifications(null)}
              PaperProps={{ sx: { width: 340, maxHeight: 400, backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.1)' } }}
            >
              <Box sx={{ p: 1.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>Notifications</Typography>
                {unreadCount > 0 && (
                  <Button size="small" startIcon={<ReadIcon />} onClick={handleMarkAllAsRead} sx={{ fontSize: '0.75rem' }}>
                    Mark all read
                  </Button>
                )}
              </Box>
              <Divider />
              {notifications.length === 0 ? (
                <MenuItem disabled sx={{ justifyContent: 'center', py: 3 }}>
                  <Typography variant="body2" color="text.secondary">No notifications yet</Typography>
                </MenuItem>
              ) : (
                notifications.map(n => (
                  <MenuItem key={n.id} onClick={() => handleMarkAsRead(n.id)} sx={{ whiteSpace: 'normal', display: 'block', borderBottom: '1px solid rgba(255,255,255,0.05)', backgroundColor: n.readAt ? 'transparent' : 'rgba(46, 125, 91, 0.1)' }}>
                    <Typography variant="subtitle2" sx={{ color: n.readAt ? 'text.secondary' : '#f2b84b', fontSize: '0.85rem' }}>
                      {n.title}
                    </Typography>
                    <Typography variant="body2" sx={{ fontSize: '0.75rem', mt: 0.5, color: n.readAt ? 'rgba(255,255,255,0.5)' : '#fff' }}>
                      {n.message}
                    </Typography>
                  </MenuItem>
                ))
              )}
            </Menu>

            {/* Logout */}
            <Tooltip title="Log out">
              <IconButton color="error" onClick={logout}>
                <LogoutIcon />
              </IconButton>
            </Tooltip>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
}
