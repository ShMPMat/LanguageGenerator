# Language generator

This is my passion project for generating artificial languages. It procedurally
generates phonetics, lexis, inflection, derivation and syntax.

## Usage

You can package the project with 

    ./gradlew jar 

and run with

    java -jar ./build/libs/LanguageGenerator.jar <SEED> 

to get the grammar and samples of a randomly generated language. If no seed is provided,
the default seed will be used. **Important:** your shell should support Unicode,
otherwise some symbols may be displayed incorrectly.

You can also use `shmp.lang.generator.LanguageGenerator` to access the generator and 
`shmp.lang.Visualizer` to access the default way to visualize generated languages.

## Contacts

[matveyshnytkin@gmail.com](mailto:matveyshnytkin@gmail.com)