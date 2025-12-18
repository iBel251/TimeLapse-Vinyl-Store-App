package org.yearup.controllers;

import org.junit.jupiter.api.Test;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Plain unit test for ProductsController,
// verifying that updateProduct uses productDao.update(...) and not create(...)
class ProductsControllerTest
{
    @Test
    void updateProduct_callsUpdateAndNotCreate()
    {
        // arrange
        ProductDao productDao = mock(ProductDao.class);

        // create the controller with the mocked DAO
        ProductsController controller = new ProductsController(productDao);

        int id = 42;
        Product product = new Product(); // fill in fields if needed

        // act
        controller.updateProduct(id, product);

        // assert
        // verify that update was called with the correct arguments
        verify(productDao, times(1)).update(id, product);

        // verify that create was never called
        verify(productDao, never()).create(any(Product.class));
    }
}