/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package io.reactivex.rxjava3.observable;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.observable.ObservableCovarianceTest.*;

public class ObservableReduceTests extends RxJavaTest {

    @Test
    public void reduceIntsObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3);
        int value = o.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toObservable().blockingSingle();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjectsObservable() {
        Observable<Movie> horrorMovies = Observable.<Movie>just(new HorrorMovie());
        Observable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Observable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toObservable();
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjectsObservable() {
        Observable<Movie> horrorMovies = Observable.<Movie>just(new HorrorMovie());
        Observable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toObservable();
        assertNotNull(reduceResult2);
    }

    @Test
    public void reduceInts() {
        Observable<Integer> o = Observable.just(1, 2, 3);
        int value = o.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).blockingGet();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjects() {
        Observable<Movie> horrorMovies = Observable.<Movie>just(new HorrorMovie());
        Observable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjects() {
        Observable<Movie> horrorMovies = Observable.<Movie>just(new HorrorMovie());
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceCovariance() {
        // must type it to <Movie>
        Observable<Movie> horrorMovies = Observable.<Movie>just(new HorrorMovie());
        libraryFunctionActingOnMovieObservables(horrorMovies);
    }

    /*
     * This accepts <Movie> instead of <? super Movie> since `reduce` can't handle covariants
     */
    public void libraryFunctionActingOnMovieObservables(Observable<Movie> obs) {
        obs.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceIntsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceIntsObservable, this.description("reduceIntsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjectsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithObjectsObservable, this.description("reduceWithObjectsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjectsObservable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithCovariantObjectsObservable, this.description("reduceWithCovariantObjectsObservable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceInts() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceInts, this.description("reduceInts"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjects() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithObjects, this.description("reduceWithObjects"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjects() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceWithCovariantObjects, this.description("reduceWithCovariantObjects"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceCovariance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reduceCovariance, this.description("reduceCovariance"));
        }

        private ObservableReduceTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableReduceTests();
        }

        @java.lang.Override
        public ObservableReduceTests implementation() {
            return this.implementation;
        }
    }
}