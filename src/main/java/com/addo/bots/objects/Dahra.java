package com.addo.bots.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Dahra implements Serializable {
  @JsonProperty("name")
  private final String name;
  @JsonProperty("in")
  private final Set<Integer> in;
  @JsonProperty("out")
  private final Set<Integer> out;

  @JsonCreator
  public Dahra(@JsonProperty("name") String name, @JsonProperty("in") Set<Integer> in, @JsonProperty("out") Set<Integer> out) {
    this.name = name;
    this.in = in;
    this.out = out;
  }

  public static Dahra newDahra(String name) {
    return new Dahra(name, new HashSet<>(), new HashSet<>());
  }

  public boolean in(Integer id) {
    out.remove(id);
    return in.add(id);
  }

  public boolean out(Integer id) {
    in.remove(id);
    return out.add(id);
  }

  public String getName() {
    return name;
  }

  public Set<Integer> getIn() {
    return in;
  }

  public Set<Integer> getOut() {
    return out;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Dahra dahra = (Dahra) o;
    return Objects.equals(name, dahra.name) &&
        Objects.equals(in, dahra.in) &&
        Objects.equals(out, dahra.out);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, in, out);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("in", in)
        .add("out", out)
        .toString();
  }
}
