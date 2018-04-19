The main purpose of this program is to provide command line access to the Oxford Dictionary.
As a secondary function, the program should log the queries it receives, alerting the user when they are repeating queries.
Ideally this will enable the queries to be leveraged to help users expand their vocabulary, or at the very least insult them.

On a personal level, the development of this program is an opportunity to utilize and extend the teachings from the two databases courses I've recently taken.
This is my first foray into incorporating a database into a java program.

Oxford dictionary API Schema and relevant information available at https://developer.oxforddictionaries.com/documentation 


<h1>Completed Objectives</h1>

    -Retrieval from the Oxford Dictionary API is fully implemented.
    -MySQL database implemented and fully functional.



<h3>TO DO:</h3>
<h5>WordUp.class</h5>
    
    -Increase the error handling of mis-spelled queries (concatenate compound words with '-', etc.)
    -Sync up with my Joernal app to log queried terms in daily entries?
    -Maybe open up the definition in a new window for prettier display?
    
<h5>WordUpTest.class</h5>
    
    -Refactor tests to match the recent refactoring of WordUp.class 

<h5>MySQLConnector.class</h5>
    
    -Check for existence of MySQL on the system and 'wordup' database
    -Implement cascading deletion, so removing a definition removes all sub-definitions and examples (optional).
