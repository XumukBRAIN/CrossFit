package com.example.crossFit.service;

import com.example.crossFit.exceptions.ResourceAlreadyIsRegisteredException;
import com.example.crossFit.exceptions.ResourceNotFoundException;
import com.example.crossFit.model.entity.Client;
import com.example.crossFit.model.entity.Item;
import com.example.crossFit.model.entity.Orders;
import com.example.crossFit.repository.ClientRepo;
import com.example.crossFit.repository.ItemRepo;
import com.example.crossFit.repository.OrdersRepo;
import com.example.crossFit.response.SuccessResponse;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepo clientRepo;
    private final OrdersRepo ordersRepo;
    private final ItemRepo itemRepo;
    private final PasswordEncoder passwordEncoder;

    private final static String SUBJECT = "Ваш заказ c номером создан!";
    private final static String TEXT = "! Благодарим за выбор нашего магазина! Вашему заказу присвоен номер: ";
    private final static String URL = "jdbc:postgresql://containers-us-west-182.railway.app:6020/railway";
    private final static String USERNAME = "postgres";
    private final static String PASSWORD = "ewjAAeTjhj8XsUgEJNDe";

    @Value("${fitness.mail.username}")
    private String mail;

    /**
     * Сервис по отправке писем
     */
    private JavaMailSender mailSender;

    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public ClientService(ClientRepo clientRepo, OrdersRepo ordersRepo, ItemRepo itemRepo, PasswordEncoder passwordEncoder) {
        this.clientRepo = clientRepo;
        this.ordersRepo = ordersRepo;
        this.itemRepo = itemRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_COACH')")
    @Transactional(readOnly = true)
    public Client getVisitor(UUID id) {
        Client client = clientRepo.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Клиент с таким id: " + id + " не зарегистрирован!");
        }
        return client;
    }


    @Transactional(readOnly = true)
    public Client findByPhoneNumber(String phoneNumber) {
        Client client = clientRepo.findByPhoneNumber(phoneNumber);
        if (client == null) {
            throw new ResourceNotFoundException("Клиент с таким номером телефона: "
                    + phoneNumber + " не зарегистрирован!");
        }
        return client;
    }


    // @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Transactional
    public SuccessResponse register(Client client) {
        if (clientRepo.findByPhoneNumber(client.getPhoneNumber()) != null) {
            throw new ResourceAlreadyIsRegisteredException("Клиент с таким номером телефона: "
                    + client.getPhoneNumber() + " уже зарегистрирован!");
        }
        if (clientRepo.findByEmail(client.getEmail()) != null) {
            throw new ResourceAlreadyIsRegisteredException("Клиент с такой электронной почтой: "
                    + client.getEmail() + " уже зарегистрирован!");
        }
        client.setBalance(BigDecimal.valueOf(0));
        client.setRole("ROLE_USER");
        client.setPassword(passwordEncoder.encode(client.getPassword()));

        clientRepo.save(client);

        return new SuccessResponse("Регистрация прошла успешно!", HttpStatus.OK.value());
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public SuccessResponse deleteClient(String phoneNumber) {
        Client client = clientRepo.findByPhoneNumber(phoneNumber);
        if (client == null) {
            throw new ResourceNotFoundException("Клиент с таким номером телефона: "
                    + phoneNumber + " не зарегистрирован!");
        }
        clientRepo.delete(client);

        return new SuccessResponse("Клиент удален!", HttpStatus.OK.value());
    }


    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Transactional
    public SuccessResponse payClient(String phoneNumber, BigDecimal money) {
        Client client = clientRepo.findByPhoneNumber(phoneNumber);
        if (client == null) {
            throw new ResourceNotFoundException("Клиент с таким номером телефона: "
                    + phoneNumber + " не зарегистрирован!");
        }
        client.setBalance(client.getBalance().add(money));

        clientRepo.save(client);

        return new SuccessResponse("Оплата прошла успешно!", HttpStatus.OK.value());
    }


    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Transactional
    public SuccessResponse createMyOrders(String phoneNumber, Integer id, String title) {
        Client client = clientRepo.findByPhoneNumber(phoneNumber);
        if (client == null) {
            throw new ResourceNotFoundException("Клиент с указанным номером телефона: "
                    + phoneNumber + " не зарегистрирован!");
        }

        Optional<Item> item = itemRepo.findById(id);
        if (!item.isPresent()) {
            throw new ResourceNotFoundException("Товар с указанным id: " + id + " не найден в магазине!");
        }

        Orders o = ordersRepo.findByClientId(client.getId());
        if (o != null) {
            o.addItem(item.get());
            o.setSum(o.getSum().add(item.get().getPrice()));
        } else {

            Orders orders = new Orders();
            orders.setSum(BigDecimal.valueOf(0));
            orders.setClientId(client.getId());
            orders.setTitle(title);
            orders.setPhoneNumber(phoneNumber);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/");
            String number;
            try {
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("select nextval('orders_number_seq')");

                if (rs.next()) {
                    number = dateFormat.format(new Date()) + StringUtils.leftPad(String.valueOf(rs.getLong("nextval")), 4, "0");
                    orders.setNumber(number);
                }

                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            orders.setSum(orders.getSum().add(item.get().getPrice()));
            orders.addItem(item.get());

            ordersRepo.save(orders);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mail);
            mailMessage.setTo(client.getEmail());
            mailMessage.setSubject(SUBJECT);
            mailMessage.setText(client.getName() + TEXT + orders.getNumber());

            mailSender.send(mailMessage);

        }

        return new SuccessResponse("Заказ успешно создан!", HttpStatus.OK.value());

    }
}

