package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("orders")
@CrossOrigin

public class OrderController {
    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrderController(OrderDao orderDao, ShoppingCartDao shoppingCartDao, UserDao userDao, ProfileDao profileDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Order checkout(Principal principal)
    {
        try
        {
            // 1. Identify current user
            String username = principal.getName();
            User user = userDao.getByUserName(username);
            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");

            int userId = user.getId();

            // 2. Get shopping cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart == null || cart.getItems().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty.");

            // 3. Get profile/address
            Profile profile = profileDao.getByUserId(userId);
            if (profile == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No profile/address on file.");

            // 4. Build order
            Order order = new Order();
            order.setUserId(userId);
            order.setDate(LocalDateTime.now());
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());


            order.setShippingAmount(new BigDecimal("6.99"));

            // 5. Save order (gets generated order_id)
            Order createdOrder = orderDao.create(order);

            // 6. Save line items
            cart.getItems().values().forEach(item -> {
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setOrderId(createdOrder.getOrderId());
                lineItem.setProductId(item.getProductId());
                lineItem.setQuantity(item.getQuantity());
                lineItem.setSalePrice(item.getProduct().getPrice());

                // compute discount as a flat amount based on discountPercent, if present
                BigDecimal discountPercent = item.getDiscountPercent();
                if (discountPercent == null) {
                    discountPercent = BigDecimal.ZERO;
                }
                BigDecimal base = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal discountAmount = base.multiply(discountPercent);
                lineItem.setDiscount(discountAmount);

                // optional: set lineTotal as a convenience for API output
                lineItem.setLineTotal(base.subtract(discountAmount));

                orderDao.addLineItem(lineItem);
            });

            // 7. Clear cart
            shoppingCartDao.clearCart(userId);

            // 8. Return created order
            return createdOrder;
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to complete checkout.");
        }
    }

}
