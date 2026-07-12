import { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Button, Grid, CircularProgress, Chip } from '@mui/material';
import { Star as StarIcon, HeartBroken as RemoveIcon } from '@mui/icons-material';
import * as wishlistService from '../services/wishlistService';

export default function WishlistSection() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFavorites();
  }, []);

  async function loadFavorites() {
    try {
      setLoading(true);
      const res = await wishlistService.getMyWishlist();
      setFavorites(res || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleRemove(businessId) {
    try {
      await wishlistService.removeFromWishlist(businessId);
      setFavorites(prev => prev.filter(b => b.id !== businessId));
    } catch (e) {
      console.error(e);
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
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>Favorite Food Businesses ({favorites.length})</Typography>
      {favorites.length === 0 ? (
        <Typography color="text.secondary">You haven't wishlisted any businesses yet. Favorite businesses to get notified when they upload fresh surplus food!</Typography>
      ) : (
        <Grid container spacing={3}>
          {favorites.map((biz) => (
            <Grid item xs={12} sm={6} md={4} key={biz.id}>
              <Card sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
                <CardContent>
                  <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <StarIcon sx={{ color: '#ffb300' }} /> {biz.businessName}
                  </Typography>
                  <Chip label={biz.businessType} size="small" variant="outlined" sx={{ mb: 2 }} />
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    📍 {biz.addressLine}, {biz.city}
                  </Typography>
                  <Button fullWidth variant="outlined" color="error" startIcon={<RemoveIcon />} onClick={() => handleRemove(biz.id)}>
                    Remove Favorite
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
