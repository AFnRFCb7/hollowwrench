package hollowwrench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.String.format;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ZERO;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Logger.getLogger;

/**
 * The application.
 **/
class Main {
    /**
     * The logger.
     **/
    static final Logger LOGGER = getLogger(Main.class.getCanonicalName());

    /**
     * Log something.
     *
     * @param level the level to log at
     * @param message the thing to log
     * @param args args for the log message
     **/
    static final void log(
                          final Level level,
                          final String message,
                          final Object ... args
                          ) {
        LOGGER.log(level, message, args);
    }

    /**
     * The number 2.
     **/
    static final BigDecimal TWO = new BigDecimal("2");

    /**
     * The program.
     *
     * <OL>
     *      <LI> Waits for User to type ready
     *      <LI> Guesses 0
     *      <LI> Uses higher, lower to find floor and ceiling.
     *      <LI> Uses modified binary search to find guess.
     * </OL>
     *
     * @param args nothing
     * @throws IOException never
     **/
    public static void
        main(final String[]args)
        throws IOException {
        // Cobertura does not seem to like ARM.
        final InputStreamReader inputStreamReader = new InputStreamReader(in);
        final BufferedReader bufferedReader =
            new BufferedReader(inputStreamReader);
        final BigDecimal positiveOne = new BigDecimal("1");
        final BigDecimal negativeOne = new BigDecimal("-1");
        final BigDecimal ten = new BigDecimal("10");
        final State identity = state(false, ZERO, ZERO, ZERO, 0);
        final BiFunction<State, ? super String, State> accumulator =
            new BiFunction
            <State, String, State>() {
                @Override
                public final State apply(
                                         final State oldState,
                                         final String string
                                         ) {
                    if ("yes".equals(string)) {
                        log(
                            FINER,
                            "The number is {0}.",
                            oldState.candidate()
                            );
                        exit(0);
                    }
                    log(FINEST, "write(\"{0}\");", string);
                    log(
                        FINER,
                        "The old state was {0}.",
                        oldState.toString()
                        );
                    log(FINER, "The user types \"{0}\".", string);
                    final boolean isReady =
                    oldState.isReady() || "ready".equals(string);
                    final BigDecimal floor;
                    final BigDecimal ceiling;
                    final BigDecimal candidate;
                    final int scale;
                    if ("higher".equals(string)) {
                        if (
                            oldState.candidate().equals(oldState.floor())
                            && oldState.candidate().equals(oldState.ceiling())
                            ) {
                            floor = oldState.floor();
                            ceiling = positiveOne;
                            candidate = positiveOne;
                            scale = 0;
                        } else
                            if (
                                oldState.candidate().equals(oldState.ceiling())
                                ) {
                                floor = oldState.candidate();
                                ceiling = oldState.ceiling().multiply(ten);
                                candidate = ceiling;
                                scale = oldState.scale() - 1;
                            } else {
                                floor = oldState.candidate();
                                ceiling = oldState.ceiling();
                                scale =
                                    oldState.scale()
                                    + (
                                       aboutEquals(
                                                   oldState.candidate(),
                                                   oldState.ceiling(),
                                                   oldState.scale())
                                       ? 1
                                       : 0
                                       );
                                candidate =
                                    (oldState.ceiling()
                                     .add(oldState.candidate())
                                     .divide(TWO, scale, ROUND_DOWN));
                            }
                    } else if ("lower".equals(string)) {
                        if
                            (
                             oldState.candidate().equals(oldState.floor())
                             && oldState.candidate().equals(oldState.ceiling())
                             ) {
                            floor = negativeOne;
                            ceiling = oldState.ceiling();
                            candidate = negativeOne;
                            scale = 0;
                        } else
                            if (oldState.candidate().equals(oldState.floor())) {
                                floor = oldState.floor().multiply(ten);
                                ceiling = oldState.candidate();
                                candidate = floor;
                                scale = oldState.scale() - 1;
                            } else {
                                floor = oldState.floor();
                                ceiling = oldState.candidate();
                                scale =
                                oldState.scale()
                                + (aboutEquals(
                                               oldState.floor(),
                                               oldState.candidate(),
                                               oldState.scale()
                                               )
                                   ? 1
                                   : 0);
                                candidate =
                                (oldState.floor().add(oldState.candidate())
                                 .divide(TWO, scale, ROUND_DOWN));
                            }
                    } else {
                        floor = oldState.floor();
                        ceiling = oldState.ceiling();
                        candidate = oldState.candidate();
                        scale = oldState.scale();
                    }
                    if (isReady) {
                        log(
                            FINEST,
                            "assertEquals(\"Is the number {0}?\", read());",
                            candidate.toPlainString()
                            );
                        out.println(
                                    format(
                                           "Is the number %1$s?",
                                           candidate.toPlainString()
                                           )
                                    );
                    }
                    final State newState =
                    state(isReady, floor, ceiling, candidate, scale);
                    log(
                        FINER,
                        "The new state will be {0}",
                        newState.toString()
                        );
                    return newState;
                }
            };
        // What is the point of the combiner?
        final BinaryOperator<State> combiner = new BinaryOperator<State>() {
                @Override
                public State apply(
                                   final State a,
                                   final State b
                                   ) {
                    return b;
                }
            };
        bufferedReader.lines().reduce(identity, accumulator, combiner);
    }

    /**
     * The current state of the game.
     **/
    interface State {
        /**
         * Is the game on?
         *
         * @return true iff the game is on
         **/
        boolean isReady();

        /**
         * A floor on guesses.
         *
         * Initially this is not a true floor on the guesses.
         * We won't know the floor
         * until the user types "higher" for the first time.
         *
         * @return a floor
         **/
        BigDecimal floor();

        /**
         * A ceiling on guesses.
         *
         * Initially this is is not a true ceiling on the guesses.
         * We won't know the ceiling
         * until the user types "lower" for the first time.
         *
         * @return a ceiling
         **/
        BigDecimal ceiling();

        /**
         * The current guess.
         *
         * @return the guess
         **/
        BigDecimal candidate();

        /**
         * The place at which we are currently comparing.
         * Initially the scale is 0 (arbitrary).
         *
         * The the floor or ceiling will grow (in magnitude)
         * until we bound the guess.
         * As the floor or ceiling grows, the scale grows.
         *
         * Once the guess is bounded,
         * we start to shrink the scale again, until the user
         * types "yes".
         *
         * @return the scale
         **/
        int scale();
    };

    /**
     * Constructs a State.
     *
     * @param isReady ready status
     * @param floor a floor for guesses
     * @param ceiling a ceiling for guesses
     * @param candidate a guess
     * @param scale we are accurate to the scale
     * @return a State object
     **/
    static State state(
                       final boolean isReady,
                       final BigDecimal floor,
                       final BigDecimal ceiling,
                       final BigDecimal candidate,
                       final int scale
                       ) {
        return new State() {
            /**
             * {@inheritDoc}.
             **/
            @Override
            public final boolean isReady() {
                return isReady;
            }

            /**
             * {@inheritDoc}.
             **/
            @Override
            public final BigDecimal floor() {
                return floor;
            }

            /**
             * {@inheritDoc}.
             **/
            @Override
            public final BigDecimal ceiling() {
                return ceiling;
            }

            /**
             * {@inheritDoc}.
             **/
            @Override
            public final BigDecimal candidate() {
                return candidate;
            }

            /**
             * {@inheritDoc}.
             **/
            @Override
            public final int scale() {
                return scale;
            }

            /**
             * {@inheritDoc}.
             **/
            @Override
            public final String toString() {
                return
                    format(
                           "%1$s [%2$s | %3$s | %4$s ] %5$s",
                           isReady,
                           floor,
                           candidate,
                           ceiling,
                           scale
                           );
            }
        };
    }

    /**
     * Compares the two specified BigDecimal to see if they are about the same.
     *
     * @param a the first BigDecimal
     * @param b the second BigDecimal
     * @param scale
     *              -1 means equals to the to tens place,
     *               0 means equal to the ones places,
     *               1 means equal to the hundreds place
     * @return true iff the two BigDecimal are about the same
     **/
    static final boolean aboutEquals(
                                     final BigDecimal a,
                                     final BigDecimal b,
                                     final int scale
                                     ) {
        final BigDecimal diff =
            b.movePointRight(scale).subtract(a.movePointRight(scale));
        final boolean aboutEquals = 1 <= TWO.compareTo(diff);
        return aboutEquals;
    }
}
