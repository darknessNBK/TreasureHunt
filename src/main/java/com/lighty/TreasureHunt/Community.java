package com.lighty.TreasureHunt;

import de.leonhard.storage.Json;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

public class Community {

    @Getter @Setter private static String name;
    @Getter @Setter private static String contract;
    @Getter @Setter private static Json config;

    public Community(String name, String contract) {
        setName(name);
        setContract(contract);
        setConfig(new Json(new File("plugins/TreasureHunt/communities/" + name + ".json")));
        this.save();
    }

    public void save() {
        setConfig(new Json(new File("plugins/TreasureHunt/communities/" + name + ".json")));
        config.set("name", name);
        config.set("contract", contract);
    }

    public void remove() {
        Main.getCommunities().remove(this);
        config.getFile().delete();
    }

}
