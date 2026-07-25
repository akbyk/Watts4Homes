package com.akbyk.watts4homes.core.rules;

import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.sql.ResultSet;
import org.apache.ignite.sql.SqlRow;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Tuple;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HomeStateStore {

    private final IgniteClient igniteClient;
    private final RecordView<Tuple> view;

    public HomeStateStore(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
        this.view = igniteClient.tables().table("home_state").recordView();
    }

    public HomeState get(Long homeId) {
        Tuple key = Tuple.create().set("home_id", homeId);
        Tuple row = view.get(null, key);
        return row == null ? null : toHomeState(row);
    }

    public void put(Long homeId, HomeState state) {
        Tuple row = Tuple.create()
                .set("home_id", homeId)
                .set("accumulated_usage", state.getAccumulatedUsage())
                .set("accumulated_cost", state.getAccumulatedCost())
                .set("tariff_state", state.getTariffState())
                .set("budget_quota", state.getBudgetQuota())
                .set("current_rate", state.getCurrentRate())
                .set("penalty_rate", state.getPenaltyRate())
                .set("breached_eighty_percent", state.isBreachedEightyPercent())
                .set("breached_hundred_percent", state.isBreachedHundredPercent())
                .set("appliance_ids", joinIds(state.getApplianceIds()));
        view.upsert(null, row);
    }

    public List<Entry> getAll() {
        List<Entry> results = new ArrayList<>();
        try (ResultSet<SqlRow> rs = igniteClient.sql().execute(null,
                "SELECT home_id, accumulated_usage, accumulated_cost, tariff_state, budget_quota, " +
                        "current_rate, penalty_rate, breached_eighty_percent, breached_hundred_percent, appliance_ids " +
                        "FROM home_state")) {
            while (rs.hasNext()) {
                SqlRow row = rs.next();
                HomeState state = new HomeState();
                state.setAccumulatedUsage(row.doubleValue("accumulated_usage"));
                state.setAccumulatedCost(row.doubleValue("accumulated_cost"));
                state.setTariffState(row.stringValue("tariff_state"));
                state.setBudgetQuota(row.doubleValue("budget_quota"));
                state.setCurrentRate(row.doubleValue("current_rate"));
                state.setPenaltyRate(row.doubleValue("penalty_rate"));
                state.setBreachedEightyPercent(row.booleanValue("breached_eighty_percent"));
                state.setBreachedHundredPercent(row.booleanValue("breached_hundred_percent"));
                state.setApplianceIds(splitIds(row.stringValue("appliance_ids")));
                results.add(new Entry(row.longValue("home_id"), state));
            }
        }
        return results;
    }

    private HomeState toHomeState(Tuple row) {
        HomeState state = new HomeState();
        state.setAccumulatedUsage(row.doubleValue("accumulated_usage"));
        state.setAccumulatedCost(row.doubleValue("accumulated_cost"));
        state.setTariffState(row.stringValue("tariff_state"));
        state.setBudgetQuota(row.doubleValue("budget_quota"));
        state.setCurrentRate(row.doubleValue("current_rate"));
        state.setPenaltyRate(row.doubleValue("penalty_rate"));
        state.setBreachedEightyPercent(row.booleanValue("breached_eighty_percent"));
        state.setBreachedHundredPercent(row.booleanValue("breached_hundred_percent"));
        state.setApplianceIds(splitIds(row.stringValue("appliance_ids")));
        return state;
    }

    private String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Long> splitIds(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(",")).map(Long::parseLong).collect(Collectors.toList());
    }

    public record Entry(Long homeId, HomeState state) {}
}