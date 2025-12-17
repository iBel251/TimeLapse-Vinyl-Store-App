package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCartDao extends MySqlDaoBase implements ShoppingCartDao {

    private final ProductDao productDao;
    public MySqlCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId){
        String sql = "SELECT user_id, product_id, quantity FROM shopping_cart WHERE user_id = ?";
        ShoppingCart cart = new ShoppingCart();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
             statement.setInt(1,userId);

             try(ResultSet results = statement.executeQuery()){
                 while(results.next()){
                     //process each row
                     cart.add(mapRow(results));
                 }
             }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cart;

    }

    @Override
    public void addProduct(int userId, int productId){
        // if product is already in cart, increment quantity by 1
        String sql = """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE quantity = quantity + 1
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    private ShoppingCartItem mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");
        int userId = row.getInt("user_id");

        Product product = productDao.getById(productId);


        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(quantity);

        return item;
    }
}
