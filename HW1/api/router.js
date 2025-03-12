// router.js
const url = require('url');
const controllers = require('./controllers');

function router(req, res) {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method;

  // Route: GET /products -- List all products
  if (pathname === '/products' && method === 'GET') {
    return controllers.handleGetProducts(req, res);
  }

  // Route: POST /products -- Create a new product
  if (pathname === '/products' && method === 'POST') {
    return controllers.handleCreateProduct(req, res);
  }

  // Routes for endpoints with a product id: /products/:id
  if (pathname.startsWith('/products/')) {
    const parts = pathname.split('/');
    // Expecting /products/:id (parts.length === 3)
    if (parts.length === 3) {
      const id = parts[2];
      if (method === 'GET') {
        return controllers.handleGetProductById(req, res, id);
      }
      if (method === 'PUT') {
        return controllers.handleUpdateProduct(req, res, id);
      }
      if (method === 'DELETE') {
        return controllers.handleDeleteProduct(req, res, id);
      }
    }
  }

  // Route: GET /inventory/check/:productId -- Check stock
  if (pathname.startsWith('/inventory/check/')) {
    const parts = pathname.split('/');
    if (parts.length === 4 && method === 'GET') {
      const productId = parts[3];
      return controllers.handleCheckInventory(req, res, productId);
    }
  }

  // Route: PUT /inventory/update/:productId -- Update stock quantity
  if (pathname.startsWith('/inventory/update/')) {
    const parts = pathname.split('/');
    if (parts.length === 4 && method === 'PUT') {
      const productId = parts[3];
      return controllers.handleUpdateInventory(req, res, productId);
    }
  }

  // Route: GET /inventory/notify -- Get products with low stock
  if (pathname === '/inventory/notify' && method === 'GET') {
    return controllers.handleNotifyInventory(req, res);
  }

  // Default: Route not found
  res.writeHead(404, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ error: "Not Found" }));
}

module.exports = router;
