package com.akbyk.watts4homes.core.rules;

import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Tuple;
import org.springframework.stereotype.Component;

@Component
public class ApplianceBreachStore {

    private final RecordView<Tuple> view;

    public ApplianceBreachStore(IgniteClient igniteClient) {
        this.view = igniteClient.tables().table("appliance_breach").recordView();
    }

    public ApplianceBreachState get(String breachKey) {
        Tuple key = Tuple.create().set("breach_key", breachKey);
        Tuple row = view.get(null, key);
        return row == null ? null : toState(row);
    }

    public void put(String breachKey, ApplianceBreachState state) {
        Tuple row = Tuple.create()
                .set("breach_key", breachKey)
                .set("safe_limit_watts", state.getSafeLimitWatts())
                .set("consecutive_breach_count", state.getConsecutiveBreachCount())
                .set("last_status", state.getLastStatus());
        view.upsert(null, row);
    }

    private ApplianceBreachState toState(Tuple row) {
        ApplianceBreachState state = new ApplianceBreachState();
        state.setSafeLimitWatts(row.doubleValue("safe_limit_watts"));
        state.setConsecutiveBreachCount(row.intValue("consecutive_breach_count"));
        state.setLastStatus(row.stringValue("last_status"));
        return state;
    }
}