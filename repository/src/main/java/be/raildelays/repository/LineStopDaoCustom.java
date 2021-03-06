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

package be.raildelays.repository;

import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.TrainLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LineStopDaoCustom {

    /**
     * Search a list of delayed or canceled arrival {@link LineStop} which belong to departure or
     * arrival station for a certain day.
     *
     * @param date           date for which you do the search
     * @param station        departure station
     * @param delayThreshold minimum delay (in milliseconds)
     * @param request        define the paging
     * @return a sub-list (called a {@link Page}) of {@link LineStop} belonging to departure
     */
    Page<LineStop> findDepartureDelays(LocalDate date, Station station, long delayThreshold, Pageable request);

    /**
     * Search a list of delayed or canceled arrival {@link LineStop} which belong to departure or
     * arrival station for a certain day.
     *
     * @param date           date for which you do the search
     * @param station        departure station
     * @param delayThreshold minimum delay (in milliseconds)
     * @return a collection of {@link LineStop} belonging to departure
     */
    List<LineStop> findDepartureDelays(LocalDate date, Station station, long delayThreshold);

    /**
     * Search a list of delayed or canceled departure {@link LineStop} which belong to departure or
     * arrival station for a certain day.
     *
     * @param date           date for which you do the search
     * @param station        arrival station
     * @param delayThreshold minimum delay (in milliseconds)
     * @param request        define the paging
     * @return a sub-list (called a {@link Page}) of {@link LineStop} belonging to arrival
     */
    Page<LineStop> findArrivalDelays(LocalDate date, Station station, long delayThreshold, Pageable request);

    /**
     * Search a list of delayed or canceled departure {@link LineStop} which belong to departure or
     * arrival station for a certain day.
     *
     * @param date           date for which you do the search
     * @param station        arrival station
     * @param delayThreshold minimum delay (in milliseconds)
     * @return a collection of {@link LineStop} belonging to arrival
     */
    List<LineStop> findArrivalDelays(LocalDate date, Station station, long delayThreshold);

    /**
     * Search a the next trains which is expectedTime to arrive after a certain
     * time.
     *
     * @param station  for which you do a search
     * @param dateTime a line stops must be of the same day of the year of this dateTime
     *                 and must have the expectedTime departure time greater than the the
     *                 hour specified into that dateTime
     * @return a list of line stops of the same day in order of expectedTime arrival
     * time
     */
    List<LineStop> findNextExpectedArrivalTime(Station station, LocalDateTime dateTime);

    /**
     * Return the first {@link be.raildelays.domain.entities.LineStop } for a certain
     * {@link TrainLine} at a certain {@link be.raildelays.domain.entities.Station}.
     *
     * @param trainLine which stop the the <code>station</code>
     * @param station   representing the stop of the line
     * @return the first line stop from a list ascending ordered by expectedTime arrival time
     */
    LineStop findFistScheduledLine(TrainLine trainLine, Station station);

    /**
     * Search a certain line stop that belong to a train line for a certain day and a certain station.
     *
     * @param trainLine train line coming from our internal repository (we match only the routId).
     * @param station   station we are searching for based on the name given in any language
     * @param date      day of the year for which you do the search
     * @return a list of line stop
     */
    LineStop findByTrainLineAndDateAndStation(TrainLine trainLine, LocalDate date, Station station);


}