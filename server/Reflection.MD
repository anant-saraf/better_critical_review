# You may have observed last week that the API specification we gave for you to follow (i.e., the shape of incoming requests and outgoing responses) was not great for extensibility. There are, indeed, many things that could be improved. Name two.
One thing that could be improved is that because we have a generic response as the input instead of a specified data type we have to extensively check that the query parameters are valid in each handler.
This way we don't have a built-in check that all of the required parameters are present and in the right format.
Another is that when using Map<String, Object> as a response we lose the protection of the type checker and we rely on ourselves (the programmers) that we will populate the response with the right type of data.
Additionally, it complicates testing a little bit because it requires the use of type assertions after deserializing data.
