import { useState, useRef, useEffect } from 'react';
import { Box, Paper, TextField, IconButton, Typography, Avatar, CircularProgress, Chip, Button, Grid, Fade } from '@mui/material';
import { Chat as ChatIcon, Close as CloseIcon, Send as SendIcon, AutoAwesome as SparklesIcon, CheckCircle as CheckIcon, Info as InfoIcon } from '@mui/icons-material';
import * as chatbotService from '../services/chatbotService';

export default function ChatbotWidget({ onPrefill }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'bot',
      text: "Hi! I'm your AI Food Donation Assistant. 🍽️\n\nSimply write a description of the surplus food you have (e.g., '10 trays of veg pasta cooked 2 hours ago. Kept in the fridge. Best before 9 PM today. Loose container.') and I'll extract all the details for you to create a listing instantly!",
    }
  ]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputText.trim()) return;

    const userText = inputText.trim();
    setInputText('');

    // Append user message
    const userMsgId = Date.now();
    setMessages(prev => [...prev, { id: userMsgId, sender: 'user', text: userText }]);
    setLoading(true);

    try {
      const result = await chatbotService.extractDetails(userText);
      
      const botMsgId = Date.now() + 1;
      setMessages(prev => [
        ...prev,
        {
          id: botMsgId,
          sender: 'bot',
          text: "I've analyzed your description and extracted the following listing details:",
          extraction: result,
          rawDescription: userText
        }
      ]);
    } catch (err) {
      console.error(err);
      setMessages(prev => [
        ...prev,
        {
          id: Date.now() + 2,
          sender: 'bot',
          text: "Sorry, I couldn't extract the details. Please try again or type a clearer description.",
          isError: true
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyPrefill = (extraction, rawDescription) => {
    if (onPrefill) {
      onPrefill({
        ...extraction,
        description: rawDescription
      });
      // Optionally close the chatbot window
      setIsOpen(false);
    }
  };

  return (
    <>
      {/* Floating Chat Bubble Button */}
      <IconButton
        onClick={() => setIsOpen(!isOpen)}
        sx={{
          position: 'fixed',
          bottom: 24,
          right: 24,
          width: 56,
          height: 56,
          backgroundColor: '#2e7d5b',
          color: '#fff',
          boxShadow: '0 8px 32px rgba(46, 125, 91, 0.4)',
          zIndex: 1000,
          transition: 'transform 0.3s ease, background-color 0.2s',
          '&:hover': {
            backgroundColor: '#246347',
            transform: 'scale(1.1) rotate(5deg)',
          }
        }}
      >
        {isOpen ? <CloseIcon /> : <ChatIcon />}
      </IconButton>

      {/* Conversational Window */}
      <Fade in={isOpen}>
        <Paper
          elevation={12}
          sx={{
            position: 'fixed',
            bottom: 96,
            right: 24,
            width: { xs: 'calc(100% - 48px)', sm: 380, md: 420 },
            height: 520,
            borderRadius: 4,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            zIndex: 1000,
            backgroundColor: 'rgba(24, 29, 28, 0.95)',
            backdropFilter: 'blur(12px)',
            border: '1px solid rgba(255,255,255,0.08)',
            boxShadow: '0 12px 40px rgba(0,0,0,0.6)',
          }}
        >
          {/* Header */}
          <Box sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.08)', background: 'linear-gradient(90deg, #181d1c 0%, #2e7d5b 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Avatar sx={{ bgcolor: '#2e7d5b', width: 36, height: 36, border: '1px solid rgba(255,255,255,0.2)' }}>
                <SparklesIcon sx={{ fontSize: 20, color: '#f2b84b' }} />
              </Avatar>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#fff', fontSize: '0.95rem', lineHeight: 1.2 }}>AI Food Donation Assistant</Typography>
                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.75rem' }}>Extracts surplus details in seconds</Typography>
              </Box>
            </Box>
            <IconButton size="small" onClick={() => setIsOpen(false)} sx={{ color: '#fff' }}>
              <CloseIcon sx={{ fontSize: 20 }} />
            </IconButton>
          </Box>

          {/* Messages Area */}
          <Box sx={{ flexGrow: 1, p: 2, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 2, backgroundColor: 'rgba(17, 20, 20, 0.3)' }}>
            {messages.map((msg) => (
              <Box
                key={msg.id}
                sx={{
                  display: 'flex',
                  justifyContent: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                  width: '100%',
                }}
              >
                <Box
                  sx={{
                    maxWidth: '85%',
                    backgroundColor: msg.sender === 'user' ? '#2e7d5b' : 'rgba(255,255,255,0.03)',
                    color: '#fff',
                    p: 1.75,
                    borderRadius: msg.sender === 'user' ? '16px 16px 2px 16px' : '16px 16px 16px 2px',
                    border: msg.sender === 'bot' ? '1px solid rgba(255,255,255,0.06)' : 'none',
                    boxShadow: msg.sender === 'user' ? '0 4px 12px rgba(46, 125, 91, 0.2)' : 'none',
                  }}
                >
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-line', lineHeight: 1.4, fontSize: '0.85rem' }}>
                    {msg.text}
                  </Typography>

                  {/* Render Structured Extraction if present */}
                  {msg.extraction && (
                    <Box sx={{ mt: 2, pt: 1.5, borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                      <Grid container spacing={1} sx={{ mb: 2 }}>
                        {[
                          { label: 'Food Name', value: msg.extraction.foodName, icon: '🍔' },
                          { label: 'Category', value: msg.extraction.foodCategory, icon: '🏷️' },
                          { label: 'Quantity', value: msg.extraction.quantity, icon: '⚖️' },
                          { label: 'Storage', value: msg.extraction.storageType, icon: '❄️' },
                          { label: 'Type', value: msg.extraction.foodType, icon: '🌱' },
                          { label: 'Packaging', value: msg.extraction.packagingStatus, icon: '📦' },
                          { label: 'Preparation', value: msg.extraction.preparationTime, icon: '🕒' },
                          { label: 'Expiry/Deadline', value: msg.extraction.pickupDeadline, icon: '📅' },
                        ].map((field, idx) => (
                          field.value && field.value !== 'Not Provided' && (
                            <Grid item xs={6} key={idx}>
                              <Box sx={{ p: 1, backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 1.5, border: '1px solid rgba(255,255,255,0.04)', height: '100%' }}>
                                <Typography sx={{ fontSize: '0.65rem', color: 'rgba(255,255,255,0.5)', fontWeight: 'bold' }}>
                                  {field.icon} {field.label}
                                </Typography>
                                <Typography sx={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#fff', mt: 0.25, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                  {field.value}
                                </Typography>
                              </Box>
                            </Grid>
                          )
                        ))}
                      </Grid>

                      {msg.extraction.specialInstructions && msg.extraction.specialInstructions !== 'Not Provided' && (
                        <Box sx={{ p: 1, mb: 2, backgroundColor: 'rgba(242, 184, 75, 0.05)', borderRadius: 1.5, border: '1px solid rgba(242, 184, 75, 0.1)' }}>
                          <Typography sx={{ fontSize: '0.65rem', color: '#f2b84b', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <InfoIcon sx={{ fontSize: 10 }} /> Special Instructions
                          </Typography>
                          <Typography sx={{ fontSize: '0.72rem', color: '#fff', mt: 0.25, fontStyle: 'italic' }}>
                            {msg.extraction.specialInstructions}
                          </Typography>
                        </Box>
                      )}

                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 1.5 }}>
                        <Chip
                          label={`Confidence: ${msg.extraction.confidence}%`}
                          size="small"
                          sx={{
                            height: 20,
                            fontSize: '0.65rem',
                            backgroundColor: parseInt(msg.extraction.confidence) > 80 ? 'rgba(46, 125, 91, 0.2)' : 'rgba(242, 184, 75, 0.2)',
                            color: parseInt(msg.extraction.confidence) > 80 ? '#81c784' : '#f2b84b',
                            border: '1px solid currentColor',
                          }}
                        />
                        <Button
                          variant="contained"
                          size="small"
                          color="secondary"
                          onClick={() => handleApplyPrefill(msg.extraction, msg.rawDescription)}
                          startIcon={<CheckIcon />}
                          sx={{
                            fontSize: '0.72rem',
                            textTransform: 'none',
                            fontWeight: 'bold',
                            px: 1.5,
                            py: 0.5,
                            borderRadius: 2,
                            color: '#111414',
                            '&:hover': {
                              backgroundColor: '#e0a33a',
                            }
                          }}
                        >
                          Use Details to List
                        </Button>
                      </Box>
                    </Box>
                  )}
                </Box>
              </Box>
            ))}

            {loading && (
              <Box sx={{ display: 'flex', justifyContent: 'flex-start', width: '100%' }}>
                <Box sx={{ p: 1.5, borderRadius: '16px 16px 16px 2px', backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.06)', display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CircularProgress size={16} sx={{ color: '#2e7d5b' }} />
                  <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.75rem' }}>AI is extracting details...</Typography>
                </Box>
              </Box>
            )}
            <div ref={messagesEndRef} />
          </Box>

          {/* Input Form */}
          <Box
            component="form"
            onSubmit={handleSend}
            sx={{
              p: 1.5,
              display: 'flex',
              gap: 1,
              borderTop: '1px solid rgba(255,255,255,0.08)',
              backgroundColor: '#181d1c'
            }}
          >
            <TextField
              fullWidth
              size="small"
              placeholder="Describe your surplus food..."
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              disabled={loading}
              autoComplete="off"
              sx={{
                '& .MuiInputBase-root': {
                  fontSize: '0.85rem',
                  color: '#fff',
                  borderRadius: 3,
                  backgroundColor: 'rgba(255,255,255,0.02)',
                  '& fieldset': {
                    borderColor: 'rgba(255,255,255,0.1)',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(255,255,255,0.2)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#2e7d5b',
                  }
                }
              }}
            />
            <IconButton type="submit" disabled={!inputText.trim() || loading} sx={{ color: '#2e7d5b', '&.Mui-disabled': { color: 'rgba(255,255,255,0.12)' } }}>
              <SendIcon sx={{ fontSize: 20 }} />
            </IconButton>
          </Box>
        </Paper>
      </Fade>
    </>
  );
}
