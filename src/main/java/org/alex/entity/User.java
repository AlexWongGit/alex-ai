package org.alex.entity;

/**
 * @description jdk17新特性 record
 * @author wangzf
 * @date 2024/6/19
 */
public record User(String name, String password) implements Comparable<User>{

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    @Override
    public int compareTo(User o) {
        return 0;
    }
}
