# Usage

  To build the project
  `ant all`
  We are expecting that the output will be
  `There was a failure in the e2e test coverage."

  This will compile the program and generate 2 reports:
  * coverage - build/e2e-test/cobertura/index.html
  * unit tests - build/e2e-test/junitreport/report/index.html

  To run the program
  `java -Djava.util.logging.config.file=logging.properties -jar build/dist/hollowwrench.jar`

  


# Algorithm
     The State object keeps track of whether the user has previously typed "ready".
     The program does not attempt to guess the number before the user types "ready".

     The program maintains three number variables:  floor, ceiling, and candidate.

     The program asks the user if candidate is the number.

     Initially floor, ceiling, and candidate are set to 0.
     When the user first answers "higher" or "lower", the program knows whether the
     number is positive or negative.
     
     If the user first types "higher", the program knows the number is positive and
     changes the variables to floor=0, ceiling=1, and candidate=1.
     At this point, the floor is a true floor; but the ceiling is not a true ceiling.

     While the user keeps answering "higher", the program keeps multiplying the ceiling and candidate by 10, until the user answer "lower".
     After the user answers "lower", the program knows the floor is a true floor and the ceiling is a true ceiling.

     Then the program can use a modification of binary search to find the user's number.

     When testing the program, I found that I picked numbers like 7.5.
     Using binary search was frustrating because the sequence of candidate would be 0, 1, 10, (1+10)/2=6.5, (6.5+10)/2=8.25, (6.5+8.25)/2=7.375, (7.375_8.25)/2=7.8125, ...
     I did not like it because the guesses are getting increasingly longer.
     
     So my algorithm keeps track of the scale of the candidate.
     
# Discussion
  This project was an opportunity to try some new technologies.
  ## Streams and functional orientation
     The project code is functional and not imperative.
     It uses the Stream.reduce method to operate on the users input one String at a time.
     I created an identity object which represents the initial State (The computer has no clue what the user's number is).
     Reduce computes a new State based on each new String and the previous State (starting with the Identity state).
