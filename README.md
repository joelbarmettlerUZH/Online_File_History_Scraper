# File History Scraper - Update and Archive every Version of an online File!

The Java File History Scraper let's you update a local file according to some File you have found online. Every time a new change to the online file is introduced, the FileHistory Scraper will recognize that change, archive the file he has already got locally and download the new File to the lokal disk. Through this process, you always have the newest version of the Online File offline and can keep track of every version ever existed online!

# Overview
The FileHistory Scraper was build with the following key-functionalities in mind

  - Download an online file to your local disk
  - Continuously compare the only fine according to a similarity algorithm
  - Archive and update local files when a change is introduces online beyond a certain treshold


## Usage

In the following paragraphs, I am going to describe how you can get and use the FileHistory Scraper for your own projects. The Program works best with text-like documents such as PDF, HyperText or Word, it is not optimized for recognizing change in Images or other non-text Media. However, by implementing your own Similarity Algorithm, introducing the ability to efficiently compare image similarities should not lead to problems. 

###  Getting it

To get the source code of the FileHistory Scraper, Fork the github repo to your local disk and place the .java classes into your project directory.
[You find the Source code here](https://github.com/joelbarmettlerUZH/Online_File_History_Scraper)

### Create a new File

The FileHistory Scraper is all about the Class LocalFile. First, create an instance of LocalFile with two parameters: Where you want to store the file offline and where it is to find online:

```Java
LocalFile testfile = new LocalFile("./Downloads/whitepaper/substratum_whitepaper.pdf", "http://substratum.net/wp-content/uploads/2017/08/substratum_whitepaper.pdf");
```

#### Updating the File

To update a LocalFile, call the .update Method with two parameters: a percentage Treshold (*to what degree the online file can vary from the offline copy before the version is updates. Depending on the File size, a treshold of one percent = 0.01 seems to be a good value, since metadata of online files can change on the fly*), we well as the similarity algorithm you want to use. You can implement your own Similarity classes by implementing the provided **similarity.java** interface. In doubt, just pass *new SimpleSimilarity()* because this is the most efficient yet promising comparison algorithm that is provided in the package. Watch out that the update-method throws a IOException when the file is not accessible, so surround the call with an exception handler
```Java
try {
    testfile.update(0.01, new LevenshteinSimilairty());
} catch(IOException io){
    System.out.println("ERROR: File could not be accessed.");
}
```

## Custom Similarity
The FileHistory Scraper comes with two Similarity algorithms: LevenshteinSimilarity and SimpleSimilarity, while SimpleSimilarity uses LevenshteinSimilarity on certain parts of the documents to statistically compute the similarity between two documents. Note that SimpleSimilarity is good for large objects but it may oversee small changes, so when you really want to be sure to detect every little change, use LevenshteinSimilarity in trade of to the few seconds you have to wait untill the document is processed. 

To implement your own Similarity Class, implement the Similarity Interface and create a method that takes two String and a treshold, with returning a boolean indicating whether the two strings are similar to that treshold level or not. 

```Java
public interface Similarity {
    boolean similarity(String s1, String s2, double treshold);
}
```

License
----

MIT License

Copyright (c) 2018 Joel Barmettler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


