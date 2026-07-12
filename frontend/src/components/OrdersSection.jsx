import { useEffect, useState } from 'react';
import { Box, Paper, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions, Rating, TextField, CircularProgress } from '@mui/material';
import { Check as ApproveIcon, Star as ReviewIcon } from '@mui/icons-material';
import * as orderService from '../services/orderService';
import * as donationService from '../services/donationService';
import * as reviewService from '../services/reviewService';
import * as paymentService from '../services/paymentService';

export default function OrdersSection({ role }) {
  const [orders, setOrders] = useState([]);
  const [donations, setDonations] = useState([]);
  const [loading, setLoading] = useState(true);

  // Review modal states
  const [reviewOpen, setReviewOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [reviewedOrders, setReviewedOrders] = useState({});

  useEffect(() => {
    loadData();
  }, [role]);

  async function loadData() {
    try {
      setLoading(true);
      if (role === 'ROLE_CONSUMER') {
        const orderRes = await orderService.getMyOrders();
        setOrders(orderRes.content || []);
      } else if (role === 'ROLE_NGO') {
        const donationRes = await donationService.getMyClaims();
        setDonations(donationRes.content || []);
      } else if (role === 'ROLE_BUSINESS_OWNER') {
        const orderRes = await orderService.getBusinessOrders();
        setOrders(orderRes.content || []);

        const donationRes = await donationService.getBusinessClaims();
        setDonations(donationRes.content || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleApproveDonation(donationId) {
    try {
      await donationService.approveDonation(donationId);
      loadData();
    } catch (e) {
      console.error(e);
    }
  }

  async function handleMockPayment(orderId) {
    try {
      // Simulate successful payment capture
      const intentRes = await paymentService.createPaymentIntent(orderId);
      // Wait a moment then trigger webhook sandbox endpoint
      alert(`Stripe Payment Intent Created: ${intentRes.stripePaymentIntentId}. Simulating raw payment succeeded webhook callback...`);
      
      // Simulate webhook call to backend payments webhook
      // For local development sandbox, call mock payment completion directly
      await paymentService.simulateWebhookSuccess(intentRes.stripePaymentIntentId);
      loadData();
    } catch (e) {
      console.error(e);
      alert(e.response?.data?.message || 'Error simulating payment.');
    }
  }

  async function handleCancelOrder(orderId) {
    try {
      await orderService.cancelOrder(orderId);
      loadData();
    } catch (e) {
      console.error(e);
    }
  }

  function handleOpenReview(orderId) {
    setSelectedOrderId(orderId);
    setRating(5);
    setComment('');
    setReviewOpen(true);
  }

  async function handleSubmitReview() {
    try {
      await reviewService.createReview({
        orderId: selectedOrderId,
        rating,
        comment
      });
      setReviewedOrders(prev => ({ ...prev, [selectedOrderId]: true }));
      setReviewOpen(false);
    } catch (e) {
      console.error(e);
      alert(e.response?.data?.message || 'Failed to submit review.');
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Orders Section */}
      {role !== 'ROLE_NGO' && (
        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>
            {role === 'ROLE_BUSINESS_OWNER' ? 'Customer Orders' : 'My Checkout Orders'}
          </Typography>
          {orders.length === 0 ? (
            <Typography color="text.secondary">No orders found.</Typography>
          ) : (
            <TableContainer component={Paper} sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Order ID</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Food Item</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Quantity</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Total Cost</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Status</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Pickup Code</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {orders.map((o) => (
                    <TableRow key={o.id}>
                      <TableCell>#{o.id}</TableCell>
                      <TableCell sx={{ fontWeight: 'medium' }}>{o.listingName}</TableCell>
                      <TableCell>{o.quantity}</TableCell>
                      <TableCell>${o.totalAmount?.toFixed(2)}</TableCell>
                      <TableCell>
                        <Chip label={o.status} size="small" color={o.status === 'PAID' ? 'success' : o.status === 'PENDING_PAYMENT' ? 'warning' : 'default'} />
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                        {o.status === 'PAID' || o.status === 'COMPLETED' ? o.pickupCode : '---'}
                      </TableCell>
                      <TableCell>
                        {role === 'ROLE_CONSUMER' && o.status === 'PENDING_PAYMENT' && (
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button size="small" variant="contained" onClick={() => handleMockPayment(o.id)}>Pay Sandbox</Button>
                            <Button size="small" variant="outlined" color="error" onClick={() => handleCancelOrder(o.id)}>Cancel</Button>
                          </Box>
                        )}
                        {role === 'ROLE_CONSUMER' && (o.status === 'PAID' || o.status === 'COMPLETED') && (
                          <Button size="small" variant="outlined" startIcon={<ReviewIcon />} disabled={reviewedOrders[o.id]} onClick={() => handleOpenReview(o.id)}>
                            {reviewedOrders[o.id] ? 'Reviewed' : 'Review'}
                          </Button>
                        )}
                        {role === 'ROLE_BUSINESS_OWNER' && o.status === 'PENDING_PAYMENT' && (
                          <Button size="small" variant="outlined" color="error" onClick={() => handleCancelOrder(o.id)}>Cancel</Button>
                        )}
                        {o.status !== 'PENDING_PAYMENT' && o.status !== 'PAID' && o.status !== 'COMPLETED' && 'None'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Box>
      )}

      {/* NGO Donations Claims Section */}
      {role !== 'ROLE_CONSUMER' && (
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>
            {role === 'ROLE_BUSINESS_OWNER' ? 'NGO Donation Claims' : 'My Donation Claims'}
          </Typography>
          {donations.length === 0 ? (
            <Typography color="text.secondary">No claims found.</Typography>
          ) : (
            <TableContainer component={Paper} sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Claim ID</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Food Item</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Quantity</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>NGO Org</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Status</TableCell>
                    <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Conf Code</TableCell>
                    {role === 'ROLE_BUSINESS_OWNER' && <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Action</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {donations.map((d) => (
                    <TableRow key={d.id}>
                      <TableCell>#{d.id}</TableCell>
                      <TableCell sx={{ fontWeight: 'medium' }}>{d.listingName}</TableCell>
                      <TableCell>{d.quantity}</TableCell>
                      <TableCell>{d.ngoName}</TableCell>
                      <TableCell>
                        <Chip label={d.status} size="small" color={d.status === 'APPROVED' ? 'success' : 'default'} />
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                        {d.status === 'APPROVED' ? d.confirmationCode : '---'}
                      </TableCell>
                      {role === 'ROLE_BUSINESS_OWNER' && (
                        <TableCell>
                          {d.status === 'CLAIMED' && (
                            <Button size="small" variant="contained" color="success" startIcon={<ApproveIcon />} onClick={() => handleApproveDonation(d.id)}>
                              Approve Claim
                            </Button>
                          )}
                          {d.status !== 'CLAIMED' && 'Processed'}
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Box>
      )}

      {/* Review Modal Dialog */}
      <Dialog open={reviewOpen} onClose={() => setReviewOpen(false)} PaperProps={{ sx: { backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.1)' } }}>
        <DialogTitle sx={{ fontWeight: 'bold' }}>Submit Quality Review</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1, minWidth: 320 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="body1">Rating:</Typography>
              <Rating value={rating} onChange={(_, val) => setRating(val || 5)} precision={1} />
            </Box>
            <TextField fullWidth label="Comment / Feedback" required multiline rows={3} value={comment} onChange={(e) => setComment(e.target.value)} size="small" />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReviewOpen(false)}>Cancel</Button>
          <Button onClick={handleSubmitReview} variant="contained" color="primary">Submit Review</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
