The purpose of this is to provide command line access to the Oxford Dictionary.
This dictionary should log the queries it receives, alerting the user when they are repeating queries ("you should know this word by now, stupid").
Ideally this will enable the queries to be leveraged to help users expand their vocabulary, or at the very least insult them.

Oxford dictionary API Schema and relevant information available at https://developer.oxforddictionaries.com/documentation 


TODO
    
    -Major clean up of code needed, it's getting convoluted.
    -Create a MySQL database to store the information (using JDBC)
    -Increase the error handling of mis-spelled queries
    -Sync up with journal app to log queried terms in daily entries
    




