package com.vibevault.userservice.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@Profile("seed")
@Order(10) // Run after DatabaseInitializer (default order)
@RequiredArgsConstructor
public class UserDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final int TARGET_USERS = 10_000;
    private static final int BATCH_SIZE = 1_000;

    private static final String SEED_PASSWORD = "Seed@1234";
    private static final int BCRYPT_STRENGTH = 14;

    // Role distribution
    private static final int ADMIN_COUNT = 10;
    private static final int SELLER_COUNT = 2_000;
    private static final int CUSTOMER_COUNT = 7_890;
    private static final int SUPPORT_COUNT = 100;

    private static final String[] FIRST_NAMES = {
            "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun",
            "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
            "Ananya", "Diya", "Myra", "Sara", "Aadhya",
            "Isha", "Kiara", "Riya", "Priya", "Neha",
            "Rohan", "Karan", "Amit", "Rahul", "Vikram",
            "Meera", "Pooja", "Nisha", "Shreya", "Tanvi",
            "Dev", "Raj", "Aryan", "Kabir", "Shaurya",
            "Zara", "Aisha", "Tara", "Maya", "Lila"
    };

    private static final String[] LAST_NAMES = {
            "Sharma", "Verma", "Patel", "Kumar", "Singh",
            "Gupta", "Joshi", "Reddy", "Nair", "Iyer",
            "Mehta", "Shah", "Das", "Rao", "Chatterjee",
            "Bose", "Mukherjee", "Pillai", "Menon", "Kaur",
            "Agarwal", "Jain", "Banerjee", "Dutta", "Kapoor"
    };

    private static final String[] CITIES = {
            "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai",
            "Kolkata", "Pune", "Ahmedabad", "Jaipur", "Lucknow"
    };

    private static final String[] STATES = {
            "Maharashtra", "Delhi", "Karnataka", "Telangana", "Tamil Nadu",
            "West Bengal", "Maharashtra", "Gujarat", "Rajasthan", "Uttar Pradesh"
    };

    private static final String[] STREETS = {
            "MG Road", "Park Street", "Brigade Road", "Linking Road", "Commercial Street",
            "Connaught Place", "Banjara Hills", "Koramangala", "Andheri West", "Whitefield"
    };

    private static final String[] BIOS = {
            "Avid shopper and tech enthusiast.",
            "Loves finding unique handcrafted products.",
            "Fashion-forward trendsetter.",
            "Home decor fanatic.",
            "Fitness gear collector.",
            "Bookworm and stationery lover.",
            "Sustainable living advocate.",
            "Gadget geek and early adopter.",
            "Minimalist with an eye for quality.",
            "Always on the lookout for great deals."
    };

    @Override
    public void run(String... args) {
        log.info("=== User Data Seeder Started ===");

        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        if (userCount != null && userCount >= TARGET_USERS) {
            log.info("Users table already has {} rows, skipping seed.", userCount);
            return;
        }

        long start = System.currentTimeMillis();

        // Pre-compute BCrypt hash once (expensive at strength 14)
        log.info("Computing BCrypt hash (strength {})...", BCRYPT_STRENGTH);
        String hashedPassword = new BCryptPasswordEncoder(BCRYPT_STRENGTH).encode(SEED_PASSWORD);
        log.info("BCrypt hash computed.");

        Map<String, byte[]> roleIds = ensureRoles();
        List<UserRecord> users = seedUsers(hashedPassword);
        seedUserRoles(users, roleIds);
        seedUserProfiles(users);
        seedAddresses(users);

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("=== User Data Seeder Completed in {} seconds ===", elapsed);
    }

    private Map<String, byte[]> ensureRoles() {
        String[] roleNames = {"ADMIN", "SELLER", "CUSTOMER", "SUPPORT"};
        String[] roleDescriptions = {
                "System administrator",
                "Product seller",
                "Regular customer",
                "Customer support agent"
        };

        Timestamp now = Timestamp.from(Instant.now());
        Map<String, byte[]> roleMap = new HashMap<>();

        for (int i = 0; i < roleNames.length; i++) {
            List<byte[]> existing = jdbcTemplate.query(
                    "SELECT id FROM roles WHERE name = ?",
                    (rs, rowNum) -> rs.getBytes("id"),
                    roleNames[i]
            );

            if (!existing.isEmpty()) {
                roleMap.put(roleNames[i], existing.get(0));
            } else {
                byte[] id = uuidToBytes(UUID.randomUUID());
                jdbcTemplate.update(
                        "INSERT INTO roles (id, created_at, last_modified_at, is_deleted, version, name, description) " +
                        "VALUES (?, ?, ?, ?, 0, ?, ?)",
                        id, now, now, false, roleNames[i], roleDescriptions[i]
                );
                roleMap.put(roleNames[i], id);
            }
        }

        log.info("Roles ensured: {}", roleMap.keySet());
        return roleMap;
    }

    private List<UserRecord> seedUsers(String hashedPassword) {
        Long existingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        int alreadyInserted = existingCount != null ? existingCount.intValue() : 0;
        int remaining = TARGET_USERS - alreadyInserted;

        if (remaining <= 0) {
            log.info("Users already at target count.");
            return Collections.emptyList();
        }

        log.info("Seeding {} users ({} already exist)...", remaining, alreadyInserted);

        String sql = "INSERT INTO users (id, created_at, last_modified_at, is_deleted, version, " +
                "first_name, last_name, email, password, phone_number) " +
                "VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, ?)";

        Timestamp now = Timestamp.from(Instant.now());
        List<UserRecord> allUsers = new ArrayList<>(remaining);
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int totalInserted = 0;

        for (int i = 0; i < remaining; i++) {
            int idx = alreadyInserted + i;
            String firstName = FIRST_NAMES[idx % FIRST_NAMES.length];
            String lastName = LAST_NAMES[idx % LAST_NAMES.length];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + idx + "@vibevault.test";
            String phone = String.format("98000%05d", idx);
            byte[] id = uuidToBytes(UUID.randomUUID());

            allUsers.add(new UserRecord(id, idx));
            batch.add(new Object[]{id, now, now, false, firstName, lastName, email, hashedPassword, phone});

            if (batch.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                totalInserted += batch.size();
                batch.clear();
                if (totalInserted % 5_000 == 0) {
                    log.info("Users progress: {}/{}", totalInserted, remaining);
                }
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            totalInserted += batch.size();
        }

        log.info("Inserted {} users.", totalInserted);
        return allUsers;
    }

    private void seedUserRoles(List<UserRecord> users, Map<String, byte[]> roleIds) {
        if (users.isEmpty()) return;

        log.info("Seeding user roles...");
        Timestamp now = Timestamp.from(Instant.now());

        String sql = "INSERT INTO user_roles (id, created_at, last_modified_at, is_deleted, version, " +
                "user_id, role_id, assigned_at) VALUES (?, ?, ?, ?, 0, ?, ?, ?)";

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        for (UserRecord user : users) {
            String role = assignRole(user.index);
            byte[] roleId = roleIds.get(role);

            batch.add(new Object[]{
                    uuidToBytes(UUID.randomUUID()),
                    now, now, false,
                    user.id, roleId, now
            });

            if (batch.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                count += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            count += batch.size();
        }

        log.info("Inserted {} user_roles.", count);
    }

    private void seedUserProfiles(List<UserRecord> users) {
        if (users.isEmpty()) return;

        log.info("Seeding user profiles (every 5th user)...");
        Timestamp now = Timestamp.from(Instant.now());

        String sql = "INSERT INTO user_profiles (id, created_at, last_modified_at, is_deleted, version, " +
                "user_id, bio, profile_picture) VALUES (?, ?, ?, ?, 0, ?, ?, ?)";

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        for (UserRecord user : users) {
            if (user.index % 5 != 0) continue;

            String bio = BIOS[user.index % BIOS.length];
            batch.add(new Object[]{
                    uuidToBytes(UUID.randomUUID()),
                    now, now, false,
                    user.id, bio, null
            });

            if (batch.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                count += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            count += batch.size();
        }

        log.info("Inserted {} user_profiles.", count);
    }

    private void seedAddresses(List<UserRecord> users) {
        if (users.isEmpty()) return;

        log.info("Seeding addresses (every 3rd user)...");
        Timestamp now = Timestamp.from(Instant.now());

        String sql = "INSERT INTO addresses (id, created_at, last_modified_at, is_deleted, version, " +
                "street, city, state, country, zip_code, user_id) " +
                "VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        for (UserRecord user : users) {
            if (user.index % 3 != 0) continue;

            int cityIdx = user.index % CITIES.length;
            String zipCode = String.format("%06d", 100000 + user.index);

            batch.add(new Object[]{
                    uuidToBytes(UUID.randomUUID()),
                    now, now, false,
                    STREETS[user.index % STREETS.length],
                    CITIES[cityIdx],
                    STATES[cityIdx],
                    "India",
                    zipCode,
                    user.id
            });

            if (batch.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                count += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            count += batch.size();
        }

        log.info("Inserted {} addresses.", count);
    }

    private String assignRole(int index) {
        // 0-9: ADMIN, 10-2009: SELLER, 2010-9899: CUSTOMER, 9900-9999: SUPPORT
        if (index < ADMIN_COUNT) return "ADMIN";
        if (index < ADMIN_COUNT + SELLER_COUNT) return "SELLER";
        if (index < ADMIN_COUNT + SELLER_COUNT + CUSTOMER_COUNT) return "CUSTOMER";
        return "SUPPORT";
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private record UserRecord(byte[] id, int index) {}
}
