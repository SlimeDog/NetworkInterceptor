package me.lucko.networkinterceptor.common;

import com.velocitypowered.api.command.CommandSource;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;

public interface CommonCommandSender {

    void send(String msg);

    void send(String... msgs);

    boolean hasPermission(String perm);

    public static final class Spigot implements CommonCommandSender {
        private final org.bukkit.command.CommandSender sender;

        public Spigot(org.bukkit.command.CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void send(String msg) {
            sender.sendMessage(msg);
        }

        @Override
        public void send(String... msgs) {
            sender.sendMessage(msgs);
        }

        @Override
        public boolean hasPermission(String perm) {
            return sender.hasPermission(perm);
        }

    }

    public static class Bungee implements CommonCommandSender {
        private final net.md_5.bungee.api.CommandSender sender;

        public Bungee(net.md_5.bungee.api.CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void send(String msg) {
            sender.sendMessage(new TextComponent(msg));
        }

        @Override
        public void send(String... msgs) {
            for (String msg : msgs) {
                send(msg);
            }
        }

        @Override
        public boolean hasPermission(String perm) {
            return sender.hasPermission(perm);
        }

    }

    public static class Velocity implements CommonCommandSender {
        private final CommandSource source;

        public Velocity(CommandSource source) {
            this.source = source;
        }

        @Override
        public void send(String msg) {
            source.sendMessage(Component.text(msg)); // TODO - colors
        }

        @Override
        public void send(String... msgs) {
            for (String msg : msgs) {
                send(msg);
            }
        }

        @Override
        public boolean hasPermission(String perm) {
            return source.hasPermission(perm);
        }

    }

}
