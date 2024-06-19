package org.alex;

import org.alex.entity.User;

public class Application {

    public static void main(String[] args) {
        User user = new User("alex","12345");
        System.out.println(user.name());
        System.out.println(user.password());
    }
}
