import { useState, useEffect } from 'react';
import { Box, Container, Paper, Tabs, Tab, Grid, Card, CardContent, CardMedia, Button, Typography, TextField, FormControl, InputLabel, Select, MenuItem, Slider, Checkbox, FormControlLabel, Dialog, DialogTitle, DialogContent, DialogActions, Chip, IconButton } from '@mui/material';
import { Add as AddIcon, Favorite as FavIcon, FavoriteBorder as FavBorderIcon, Fastfood as FoodIcon, LocalOffer as SaleIcon, ShoppingCart as BuyIcon } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import Navbar from '../components/Navbar';
import AnalyticsDashboard from '../components/AnalyticsDashboard';
import VerificationSection from '../components/VerificationSection';
import ComplaintsSection from '../components/ComplaintsSection';
import WishlistSection from '../components/WishlistSection';
import OrdersSection from '../components/OrdersSection';
import CreateListingModal from '../components/CreateListingModal';
import ChatbotWidget from '../components/ChatbotWidget';
import * as foodListingService from '../services/foodListingService';
import * as categoryService from '../services/categoryService';
import * as wishlistService from '../services/wishlistService';
import * as orderService from '../services/orderService';
import * as donationService from '../services/donationService';

export default function Dashboard() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState(0);

  // Browse Listings States (Consumer & NGO)
  const [listings, setListings] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [radius, setRadius] = useState(15); // default 15km
  const [vegetarian, setVegetarian] = useState(false);
  const [vegan, setVegan] = useState(false);
  const [favoritesMap, setFavoritesMap] = useState({});
  const [nearbyOnly, setNearbyOnly] = useState(false);

  // Business Owner Listings States
  const [businessListings, setBusinessListings] = useState([]);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [chatbotPrefillData, setChatbotPrefillData] = useState(null);


  function handleChatbotPrefill(data) {
    setChatbotPrefillData(data);
    setCreateModalOpen(true);
  }

  // Purchase/Claim Dialog States
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [selectedListing, setSelectedListing] = useState(null);
  const [checkoutQty, setCheckoutQty] = useState(1);
  const [cardName, setCardName] = useState('');
  const [cardNumber, setCardNumber] = useState('4242 4242 4242 4242');

  useEffect(() => {
    loadBrowseData();
  }, [search, category, radius, vegetarian, vegan, nearbyOnly]);

  useEffect(() => {
    if (user && user.roles.includes('ROLE_BUSINESS_OWNER')) {
      loadBusinessListings();
    }
  }, [user]);

  async function loadBrowseData() {
    try {
      // Load Categories
      const catRes = await categoryService.getCategories();
      setCategories(catRes || []);

      // Determine listing filter types based on role
      const listingType = user.roles.includes('ROLE_NGO') ? 'FREE_DONATION' : 'DISCOUNT_SALE';

      // Load Listings (Haversine radius query if user has coordinates and nearbyOnly is active)
      let res;
      if (nearbyOnly && user.latitude && user.longitude) {
        res = await foodListingService.getNearbyActiveFoodListings(
          user.latitude,
          user.longitude,
          radius,
          category || null,
          listingType,
          vegetarian || null,
          vegan || null,
          search || null
        );
      } else {
        res = await foodListingService.getActiveFoodListings(
          category || null,
          listingType,
          vegetarian || null,
          vegan || null,
          search || null
        );
      }
      setListings(res.content || []);

      // Load wishlists if Consumer
      if (user.roles.includes('ROLE_CONSUMER')) {
        const favs = await wishlistService.getMyWishlist();
        const fMap = {};
        favs.forEach(b => { fMap[b.id] = true; });
        setFavoritesMap(fMap);
      }
    } catch (e) {
      console.error(e);
    }
  }

  async function loadBusinessListings() {
    try {
      const res = await foodListingService.getMyFoodListings(0, 50);
      setBusinessListings(res.content || []);
    } catch (e) {
      console.error(e);
    }
  }

  async function handleToggleFavorite(businessId) {
    try {
      if (favoritesMap[businessId]) {
        await wishlistService.removeFromWishlist(businessId);
        setFavoritesMap(prev => ({ ...prev, [businessId]: false }));
      } else {
        await wishlistService.addToWishlist(businessId);
        setFavoritesMap(prev => ({ ...prev, [businessId]: true }));
      }
    } catch (e) {
      console.error(e);
    }
  }

  function handleOpenCheckout(listing) {
    setSelectedListing(listing);
    setCheckoutQty(1);
    setCardName(user.fullName);
    setCheckoutOpen(true);
  }

  async function handleConfirmCheckout() {
    try {
      if (user.roles.includes('ROLE_NGO')) {
        // Claim Donation
        await donationService.claimDonation({
          listingId: selectedListing.id,
          quantity: checkoutQty
        });
      } else {
        // Buy Discount Sale
        const order = await orderService.placeOrder({
          listingId: selectedListing.id,
          quantity: checkoutQty
        });
        alert(`Order placed successfully! Order ID: #${order.id}. Dynamic pricing: $${order.totalAmount}. Complete payment in My Orders tab.`);
      }
      setCheckoutOpen(false);
      loadBrowseData();
    } catch (e) {
      console.error(e);
      alert(e.response?.data?.message || 'Error processing request.');
    }
  }

  // Role Tab Names
  const consumerTabs = ['Browse Food', 'My Orders', 'Favorite Businesses', 'Disputes / Complaints'];
  const businessTabs = ['My Food Listings', 'Orders & Donation Claims', 'Earnings & Analytics', 'Disputes / Complaints'];
  const ngoTabs = ['Browse Food Donations', 'My Claimed Donations', 'Disputes / Complaints'];
  const adminTabs = ['Verify Profiles', 'User Disputes / Complaints', 'Platform Analytics'];

  const getTabs = () => {
    if (user.roles.includes('ROLE_ADMIN')) return adminTabs;
    if (user.roles.includes('ROLE_BUSINESS_OWNER')) return businessTabs;
    if (user.roles.includes('ROLE_NGO')) return ngoTabs;
    return consumerTabs;
  };

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#111414', pb: 6 }}>
      <Navbar onTabChange={(tab) => setActiveTab(0)} />

      <Container sx={{ mt: 4 }}>
        <Paper elevation={4} sx={{ backgroundColor: '#181d1c', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
          <Tabs value={activeTab} onChange={(_, val) => setActiveTab(val)} variant="scrollable" scrollButtons="auto" sx={{ px: 2 }}>
            {getTabs().map((tabName, index) => (
              <Tab key={index} label={tabName} sx={{ fontWeight: 'bold', py: 2 }} />
            ))}
          </Tabs>
        </Paper>

        <Box sx={{ mt: 4 }}>
          {/* CONSUMER VIEW */}
          {user.roles.includes('ROLE_CONSUMER') && (
            <>
              {activeTab === 0 && (
                <Grid container spacing={3}>
                  <Grid item xs={12} md={3}>
                    <Paper sx={{ p: 3, backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
                      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>Search Filters</Typography>
                      <TextField fullWidth label="Search keyword" value={search} onChange={(e) => setSearch(e.target.value)} size="small" sx={{ mb: 2 }} />
                      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                        <InputLabel id="category-filter-label">Category</InputLabel>
                        <Select labelId="category-filter-label" label="Category" value={category} onChange={(e) => setCategory(e.target.value)}>
                          <MenuItem value="">All Categories</MenuItem>
                          {categories.map((c) => (
                            <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                      {user.latitude && (
                        <Box sx={{ mb: 2 }}>
                          <FormControlLabel
                            control={<Checkbox checked={nearbyOnly} onChange={(e) => setNearbyOnly(e.target.checked)} />}
                            label="Search nearby only"
                          />
                          {nearbyOnly && (
                            <Box sx={{ mt: 1 }}>
                              <Typography variant="body2" sx={{ mb: 1 }}>Radius Range: <strong>{radius} km</strong></Typography>
                              <Slider value={radius} onChange={(_, val) => setRadius(val)} min={1} max={50} valueLabelDisplay="auto" />
                            </Box>
                          )}
                        </Box>
                      )}
                      <FormControlLabel control={<Checkbox checked={vegetarian} onChange={(e) => setVegetarian(e.target.checked)} />} label="Vegetarian" />
                      <FormControlLabel control={<Checkbox checked={vegan} onChange={(e) => setVegan(e.target.checked)} />} label="Vegan" />
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={9}>
                    {listings.length === 0 ? (
                      <Typography color="text.secondary" align="center">No fresh food available matching your filter settings right now.</Typography>
                    ) : (
                      <Grid container spacing={3}>
                        {listings.map((l) => (
                          <Grid item xs={12} sm={6} key={l.id}>
                            <FoodListingCard listing={l} isFavorite={favoritesMap[l.businessId]} onToggleFavorite={handleToggleFavorite} onAction={handleOpenCheckout} role={user.roles[0]} />
                          </Grid>
                        ))}
                      </Grid>
                    )}
                  </Grid>
                </Grid>
              )}
              {activeTab === 1 && <OrdersSection role={user.roles[0]} />}
              {activeTab === 2 && <WishlistSection />}
              {activeTab === 3 && <ComplaintsSection role={user.roles[0]} />}
            </>
          )}

          {/* BUSINESS OWNER VIEW */}
          {user.roles.includes('ROLE_BUSINESS_OWNER') && (
            <>
              {activeTab === 0 && (
                <Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h5" sx={{ fontWeight: 'bold' }}>My Food Listings</Typography>
                    <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateModalOpen(true)}>New Listing</Button>
                  </Box>
                  {businessListings.length === 0 ? (
                    <Typography color="text.secondary">You haven't uploaded any listings yet. Click 'New Listing' to add your surplus food items.</Typography>
                  ) : (
                    <Grid container spacing={3}>
                      {businessListings.map((l) => (
                        <Grid item xs={12} sm={6} md={4} key={l.id}>
                          <FoodListingCard listing={l} role={user.roles[0]} />
                        </Grid>
                      ))}
                    </Grid>
                  )}
                  <CreateListingModal
                    open={createModalOpen}
                    onClose={() => {
                      setCreateModalOpen(false);
                      setChatbotPrefillData(null);
                    }}
                    onSuccess={loadBusinessListings}
                    prefillData={chatbotPrefillData}
                  />
                  <ChatbotWidget onPrefill={handleChatbotPrefill} />
                </Box>
              )}
              {activeTab === 1 && <OrdersSection role={user.roles[0]} />}
              {activeTab === 2 && <AnalyticsDashboard role={user.roles[0]} />}
              {activeTab === 3 && <ComplaintsSection role={user.roles[0]} />}
            </>
          )}

          {/* NGO VIEW */}
          {user.roles.includes('ROLE_NGO') && (
            <>
              {activeTab === 0 && (
                <Grid container spacing={3}>
                  <Grid item xs={12} md={3}>
                    <Paper sx={{ p: 3, backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
                      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>Search Filters</Typography>
                      <TextField fullWidth label="Search keyword" value={search} onChange={(e) => setSearch(e.target.value)} size="small" sx={{ mb: 2 }} />
                      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                        <InputLabel id="ngo-category-filter-label">Category</InputLabel>
                        <Select labelId="ngo-category-filter-label" label="Category" value={category} onChange={(e) => setCategory(e.target.value)}>
                          <MenuItem value="">All Categories</MenuItem>
                          {categories.map((c) => (
                            <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                      {user.latitude && (
                        <Box sx={{ mb: 2 }}>
                          <FormControlLabel
                            control={<Checkbox checked={nearbyOnly} onChange={(e) => setNearbyOnly(e.target.checked)} />}
                            label="Search nearby only"
                          />
                          {nearbyOnly && (
                            <Box sx={{ mt: 1 }}>
                              <Typography variant="body2" sx={{ mb: 1 }}>Radius Range: <strong>{radius} km</strong></Typography>
                              <Slider value={radius} onChange={(_, val) => setRadius(val)} min={1} max={50} valueLabelDisplay="auto" />
                            </Box>
                          )}
                        </Box>
                      )}
                      <FormControlLabel control={<Checkbox checked={vegetarian} onChange={(e) => setVegetarian(e.target.checked)} />} label="Vegetarian" />
                      <FormControlLabel control={<Checkbox checked={vegan} onChange={(e) => setVegan(e.target.checked)} />} label="Vegan" />
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={9}>
                    {listings.length === 0 ? (
                      <Typography color="text.secondary" align="center">No free donation listings available near you right now.</Typography>
                    ) : (
                      <Grid container spacing={3}>
                        {listings.map((l) => (
                          <Grid item xs={12} sm={6} key={l.id}>
                            <FoodListingCard listing={l} onAction={handleOpenCheckout} role={user.roles[0]} />
                          </Grid>
                        ))}
                      </Grid>
                    )}
                  </Grid>
                </Grid>
              )}
              {activeTab === 1 && <OrdersSection role={user.roles[0]} />}
              {activeTab === 2 && <ComplaintsSection role={user.roles[0]} />}
            </>
          )}

          {/* ADMIN VIEW */}
          {user.roles.includes('ROLE_ADMIN') && (
            <>
              {activeTab === 0 && <VerificationSection />}
              {activeTab === 1 && <ComplaintsSection role={user.roles[0]} />}
              {activeTab === 2 && <AnalyticsDashboard role={user.roles[0]} />}
            </>
          )}
        </Box>
      </Container>

      {/* Checkout / Claim Dialog Modal */}
      {selectedListing && (
        <Dialog open={checkoutOpen} onClose={() => setCheckoutOpen(false)} PaperProps={{ sx: { backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.1)' } }}>
          <DialogTitle sx={{ fontWeight: 'bold' }}>
            {user.roles.includes('ROLE_NGO') ? 'Claim Food Donation' : 'Confirm Order Checkout'}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ pt: 1, display: 'flex', flexDirection: 'column', gap: 2, minWidth: 320 }}>
              <Typography variant="body1">Listing: <strong>{selectedListing.name}</strong></Typography>
              <Typography variant="body2" color="text.secondary">Available Stock: {selectedListing.availableQuantity} items</Typography>

              <TextField fullWidth label="Checkout Quantity" type="number" value={checkoutQty} onChange={(e) => setCheckoutQty(Math.min(selectedListing.availableQuantity, Math.max(1, parseInt(e.target.value) || 1)))} size="small" />

              {!user.roles.includes('ROLE_NGO') && (
                <>
                  <Typography variant="subtitle2" sx={{ color: '#f2b84b', mt: 1 }}>Payment Sandbox Details (Stripe mock capture)</Typography>
                  <TextField fullWidth label="Cardholder Name" value={cardName} onChange={(e) => setCardName(e.target.value)} size="small" />
                  <TextField fullWidth label="Card Number" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)} size="small" />
                </>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCheckoutOpen(false)}>Cancel</Button>
            <Button onClick={handleConfirmCheckout} variant="contained" color="primary">Confirm</Button>
          </DialogActions>
        </Dialog>
      )}
    </Box>
  );
}

function FoodListingCard({ listing, isFavorite, onToggleFavorite, onAction, role }) {
  const isBusiness = role === 'ROLE_BUSINESS_OWNER';
  const isNgo = role === 'ROLE_NGO';
  const imgUrl = listing.images && listing.images.length > 0 ? listing.images[0].imageUrl : 'https://images.unsplash.com/photo-1606787366850-de6330128bfc?q=80&w=600&auto=format&fit=crop';

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 2, position: 'relative' }}>
      <CardMedia component="img" height="160" image={imgUrl} alt={listing.name} />

      {!isBusiness && onToggleFavorite && (
        <IconButton sx={{ position: 'absolute', top: 12, right: 12, backgroundColor: 'rgba(0,0,0,0.5)', '&:hover': { backgroundColor: 'rgba(0,0,0,0.7)' } }} onClick={() => onToggleFavorite(listing.businessId)}>
          {isFavorite ? <FavIcon color="error" /> : <FavBorderIcon sx={{ color: '#fff' }} />}
        </IconButton>
      )}

      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{listing.name}</Typography>
            <Chip label={listing.listingType} size="small" color={listing.listingType === 'FREE_DONATION' ? 'secondary' : 'primary'} />
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1, minHeight: 40 }}>{listing.description}</Typography>
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            {listing.vegetarian && <Chip label="Veg" size="small" sx={{ height: 20, fontSize: '0.7rem', color: '#81c784', borderColor: '#81c784' }} variant="outlined" />}
            {listing.vegan && <Chip label="Vegan" size="small" sx={{ height: 20, fontSize: '0.7rem', color: '#4db6ac', borderColor: '#4db6ac' }} variant="outlined" />}
          </Box>
        </Box>

        <Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box>
              <Typography variant="caption" color="text.secondary">Quantity: <strong>{listing.availableQuantity}</strong></Typography>
              {listing.listingType === 'DISCOUNT_SALE' && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                  <Typography variant="body1" sx={{ fontWeight: 'bold', color: '#2e7d5b' }}>${listing.discountPrice?.toFixed(2)}</Typography>
                  <Typography variant="caption" sx={{ textDecoration: 'line-through', color: 'rgba(255,255,255,0.4)' }}>${listing.originalPrice?.toFixed(2)}</Typography>
                </Box>
              )}
            </Box>
            <Typography variant="caption" color="text.secondary" align="right">Expires: {new Date(listing.expiryTime).toLocaleDateString()}</Typography>
          </Box>

          {!isBusiness && onAction && (
            <Button fullWidth variant="contained" color="primary" startIcon={isNgo ? <FoodIcon /> : <BuyIcon />} onClick={() => onAction(listing)}>
              {isNgo ? 'Claim Donation' : 'Buy Discounted'}
            </Button>
          )}
        </Box>
      </CardContent>
    </Card>
  );
}
