package Main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Random rand = new Random();

    public synchronized Room createRoom(String hostUsername, int maxHumans, ClientHandler host) {
        String code = generateCode();
        Room room = new Room(code, maxHumans, host, hostUsername);
        rooms.put(code, room);
        System.out.println("[Room] Created: " + code + " by " + hostUsername);
        return room;
    }

    public Room getRoom(String code) {
        return rooms.get(code.toUpperCase());
    }

    public void removeRoom(String code) {
        rooms.remove(code.toUpperCase());
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(chars.charAt(rand.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (rooms.containsKey(code));
        return code;
    }

    // ── Inner class Room ──────────────────────────────────────────────
    public static class Room {

        private final String code;
        private final int maxHumans;
        private final List<ClientHandler> clients = new ArrayList<>();
        private final List<String> usernames = new ArrayList<>();
        private volatile boolean started = false;

        public Room(String code, int maxHumans, ClientHandler host, String hostName) {
            this.code = code;
            this.maxHumans = maxHumans;
            clients.add(host);
            usernames.add(hostName);
        }

        public synchronized boolean join(ClientHandler h, String username) {
            if (started || clients.size() >= maxHumans) {
                return false;
            }
            clients.add(h);
            usernames.add(username);
            return true;
        }

        public boolean isFull() {
            return clients.size() >= maxHumans;
        }

        public boolean isStarted() {
            return started;
        }

        public void markStarted() {
            started = true;
        }

        public String getCode() {
            return code;
        }

        public String getHostUsername() {
            return usernames.isEmpty() ? "" : usernames.get(0);
        }

        public int getMaxHumans() {
            return maxHumans;
        }

        public int getCurrentCount() {
            return clients.size();
        }

        public List<ClientHandler> getClients() {
            return Collections.unmodifiableList(clients);
        }

        public List<String> getUsernames() {
            return Collections.unmodifiableList(usernames);
        }

        public void broadcast(GameMessage msg) {
            for (ClientHandler h : clients) {
                if (h.isRunning()) {
                    h.send(msg);
                }
            }
        }
    }
}
