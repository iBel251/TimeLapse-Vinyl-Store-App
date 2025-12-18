package org.yearup.data.mysql;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.*;
import java.time.LocalDateTime;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public MySqlOrderDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Order create(Order order)
    {
        String sql = """
        INSERT INTO orders
            (user_id, date, address, city, state, zip, shipping_amount)
        VALUES
            (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // set values
            statement.setInt(1, order.getUserId());

            LocalDateTime date = order.getDate();
            if (date == null)
            {
                date = LocalDateTime.now();
                order.setDate(date);
            }
            statement.setTimestamp(2, Timestamp.valueOf(date));

            statement.setString(3, order.getAddress());
            statement.setString(4, order.getCity());
            statement.setString(5, order.getState());
            statement.setString(6, order.getZip());
            statement.setBigDecimal(7, order.getShippingAmount());

            statement.executeUpdate();

            // get generated order_id
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next())
            {
                order.setOrderId(keys.getInt(1));
            }

            return order;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addLineItem(OrderLineItem lineItem)
    {
        String sql = """
        INSERT INTO order_line_items
            (order_id, product_id, sales_price, quantity, discount)
        VALUES
            (?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, lineItem.getOrderId());
            statement.setInt(2, lineItem.getProductId());
            statement.setBigDecimal(3, lineItem.getSalePrice());
            statement.setInt(4, lineItem.getQuantity());
            statement.setBigDecimal(5, lineItem.getDiscount());

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


}
