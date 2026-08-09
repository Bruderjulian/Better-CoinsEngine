package su.nightexpress.excellenteconomy.tops;

import java.util.UUID;

import su.nightexpress.nightcore.util.profile.CachedProfile;
import su.nightexpress.nightcore.util.profile.PlayerProfiles;

public class TopEntry {

    private final int position;
    private final String name;
    private final UUID playerId;
    private final double balance;

    private final CachedProfile profile;

    public TopEntry(int position, String name, UUID playerId, double balance) {
        this.position = position;
        this.name = name;
        this.playerId = playerId;
        this.balance = balance;

        this.profile = PlayerProfiles.createProfile(this.playerId, name.length() > 16 ? name.substring(0, 16) : name);
    }

    public int getPosition() {
        return this.position;
    }

    public String getName() {
        return this.name;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public double getBalance() {
        return this.balance;
    }

    public CachedProfile getProfile() {
        return this.profile;
    }
}
