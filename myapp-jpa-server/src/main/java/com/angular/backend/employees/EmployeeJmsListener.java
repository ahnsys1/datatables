package com.angular.backend.employees;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeJmsListener {

    @JmsListener(destination = RabbitMQConfig.NEW_EMPLOYEE_QUEUE, containerFactory = "employeeJmsListenerContainerFactory")
    public void onEmployeeCreated(Message message) throws JMSException {
        printEvent("employee.new", message);
    }

    @JmsListener(destination = RabbitMQConfig.UPDATED_EMPLOYEE_QUEUE, containerFactory = "employeeJmsListenerContainerFactory")
    public void onEmployeeUpdated(Message message) throws JMSException {
        printEvent("employee.updated", message);
    }

    @JmsListener(destination = RabbitMQConfig.DELETED_EMPLOYEE_QUEUE, containerFactory = "employeeJmsListenerContainerFactory")
    public void onEmployeeDeleted(Message message) throws JMSException {
        printEvent("employee.deleted", message);
    }

    private void printEvent(String eventType, Message message) throws JMSException {
        String body = message instanceof TextMessage textMessage
                ? textMessage.getText()
                : message.toString();
        System.out.println("[JMS] " + eventType + " " + body);
    }
}