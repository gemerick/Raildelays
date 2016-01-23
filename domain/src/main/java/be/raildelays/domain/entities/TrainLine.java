/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Almex
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package be.raildelays.domain.entities;

import be.raildelays.location.Route;
import be.raildelays.vehicule.Train;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static java.util.Comparator.*;

/**
 * Entity defining a train line.
 * To help building this entity and as the only way to do it
 * we embedded a {@link Builder}.
 * The unity of this entity is based on its {@link #routeId}
 *
 * @author Almex
 * @see AbstractEntity
 * @implNote this class apply the Value Object pattern and is therefor immutable
 */
@Entity
@Table(
        name = "TRAIN_LINE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"SHORT_NAME"})
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TrainLine extends AbstractEntity implements Train, Route<LineStop>, Comparable<TrainLine> {

    private static final long serialVersionUID = -1527666012499664304L;

    @Column(name = "SHORT_NAME")
    private String shortName;

    @Column(name = "LONG_NAME")
    private String longName;

    @Column(name = "ROUTE_ID")
    @NotNull
    private Long routeId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTURE_ID")
    private LineStop departure;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATION_ID")
    private LineStop destination;

    protected TrainLine() {
        this.shortName = "";
        this.longName = "";
    }

    protected TrainLine(Builder builder) {
        this.shortName = builder.shortName;
        this.longName = builder.longName;
        this.routeId = builder.routeId;
        this.departure = builder.departure;
        this.destination = builder.destination;
    }

    @Override
    public String toString() {
        return new StringBuilder("TrainLine: ") //
                .append("{ ") //
                .append("id: ").append(id).append(", ") //
                .append("shortName: ").append(shortName).append(", ") //
                .append("longName: ").append(longName).append(", ") //
                .append("routeId: ").append(routeId) //
                .append(" }").toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof TrainLine) {
            result = compareTo((TrainLine) obj) == 0;
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shortName);
    }

    @Override
    public int compareTo(TrainLine trainLine) {
        return Objects.compare(this, trainLine, (lho, rho) ->
                comparing(TrainLine::getRouteId, nullsLast(naturalOrder()))
                //.thenComparing(TrainLine::getShortName, nullsLast(naturalOrder()))
                //.thenComparing(TrainLine::getLongName, nullsLast(naturalOrder()))
                        .compare(lho, rho)
        );
    }

    public static class Builder {
        private String shortName;
        private String longName;
        private Long routeId;
        private LineStop departure;
        private LineStop destination;

        /**
         * Minimal initialization constructor.
         *
         * @param routeId id of this route (see GTFS documentation)
         */
        public Builder(final Long routeId) {
            this.routeId = routeId;
        }

        /**
         * Do a copy of the given {@link TrainLine}.
         *
         * @param toCopy {@link TrainLine} to copy
         */
        public Builder(final TrainLine toCopy) {
            this.routeId = toCopy.routeId;
            this.shortName = toCopy.shortName;
            this.longName = toCopy.longName;
            this.departure = new LineStop.Builder(toCopy.departure).build();
            this.destination = new LineStop.Builder(toCopy.destination).build();
        }

        public Builder shortName(final String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder longName(final String longName) {
            this.longName = longName;
            return this;
        }

        public Builder departure(final LineStop departure) {
            this.departure = departure;
            return this;
        }

        public Builder destination(final LineStop destination) {
            this.destination = destination;
            return this;
        }

        public TrainLine build() {
            return build(true);
        }

        public TrainLine build(final boolean validate) {
            TrainLine result = new TrainLine(this);

            if (validate) {
                validate(result);
            }

            return result;
        }
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public Long getRouteId() {
        return routeId;
    }

    @Override
    public LineStop getDeparture() {
        return departure;
    }

    @Override
    public LineStop getDestination() {
        return destination;
    }

    @Override
    public String getName() {
        return "" + getRouteId();
    }
}