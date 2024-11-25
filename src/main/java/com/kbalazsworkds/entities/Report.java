package com.kbalazsworkds.entities;

import com.google.gson.annotations.SerializedName;
import lombok.NonNull;

public record Report(
    @NonNull @SerializedName("host") String host,
    @NonNull @SerializedName("icmp_ping") String icmpPing,
    @NonNull @SerializedName("tcp_ping") String tcpPing,
    @NonNull @SerializedName("trace") String trace
)
{
}
