package com.example.notetakingapp.data

object ExampleDataSource {
    val notes: List<Note> = listOf(
        Note(1, "Test Title", "Test Body"),
        Note(2, "Lorem Ipsum", "Lorem ipsum dolor sit amet, " +
                "consectetur adipiscing elit, " +
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, " +
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
                " Excepteur sint occaecat cupidatat non proident, " +
                "sunt in culpa qui officia deserunt mollit anim id est laborum."),
        Note(3, "Hello", "World"),
        Note(4, "Testing", "The 2nd Row"),
        Note(5, "And", "The 3rd"),
        Note(6, "Also gonna", "Test the resizeablility of the flexible GridLayout once it's" +
                "implemented proprely on this app, so i need this kind of long notes")
    )
}
