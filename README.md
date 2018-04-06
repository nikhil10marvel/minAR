# minAR
A small, in development, minimalistic archive format that supports xz and lzma2 compression as well basic encryption

minAR is in no way a fully fledged archive format, it is merely a format that encapsulates multiple files, compressing them in the process and finally encrypting the end result.
> The JavaDoc is [here](https://htmlpreview.github.io/?https://raw.githubusercontent.com/nikhil10marvel/minAR/master/doc/index.html)

## Basic usage

#### 1. Creating an archive
```
MinAR.toggleFlags(MinAR._default_no_enc);
MinAR.outputArchive("directory", "my_archive");
```

This creates an archive of the 'directory' called my_archive.mar.
The archive contains the compressed contents of 'directory'.

The flags toggled here, enable compression and do not support encryption.
For more information, please refer the [JavaDoc](https://htmlpreview.github.io/?https://raw.githubusercontent.com/nikhil10marvel/minAR/master/doc/index.html)

#### 2. Extracting an archive

To extract the above created archive, the following should be used:
```
MinAR.toggleFlags(MinAR._default_no_enc);
MinAR.extractArchive("my_archive.mar", "some_dir", null);
```
The same flags that were toggled on, while creating the archive must be  
toggled on, with the exception of `KEY_IS_FILE` and `KEY_IS_STRING` as  
they play no roles in the creation of the archive.

```extractArchive(...)``` method will take a third parameter, which is  
the key(type: String), but it depends on the flags set.

The key is mandatory in case of encryption (obviously) and is absolutely unnecessary if the archive is not encrypted
For more information, please refer the [JavaDoc for `extractArchive(...)`](https://htmlpreview.github.io/?https://raw.githubusercontent.com/nikhil10marvel/minAR/master/doc/io/minAR/MinAR.html#extractArchive-java.lang.String-java.lang.String-java.lang.String-) method

### Flags([`MinAR.FLAG`](https://htmlpreview.github.io/?https://raw.githubusercontent.com/nikhil10marvel/minAR/master/doc/io/minAR/MinAR.FLAG.html))
Optionally Certain flags can be set or toggled on before operations to improve the created archive.
When certain flags are activated, the files will be compressed before being archived.
When certain other flags are activated, the end archive will be encrypted.

<table>
<tr>
<td>FLAG</td><td>FUNCTION</td>
</tr>
<tr>
<td>COMPRESSED</td><td>Compresses each file before archiving</td>
</tr>
<tr>
<td>ENCRYPTED</td><td>Encrypts the generated archive</td>
</tr>
<tr>
<td>KEY_IS_FILE</td><td>Changes the thrid parameter, takes in a path to a file</td>
</tr>
<tr>
<td>KEY_IS_STRING</td>Changes the third parameter, takes the string form of the key</td>
</tr>
</table>

## Code Example
```java
package test;

import io.minAR.MinAR;

public class Test {
    
    public static void main(String[] args){
        if(args.length > 0){
            String archive_name = args[1];
            if(args[0].equals("-a")){
                String diriectory_to_be_archive = args[2];
                MinAR.toggleFlags(MinAR._default_no_enc);
                MinAR.outputArchive(diriectory_to_be_archive, archive_name);
            } else if(args[0].equals("-e")){
                String extract_directory = args[2];
                MinAR.toggleFlags(MinAR._default_no_enc);
                MinAR.extractArchive(archive_name, extract_directory, null);
            }
        }
    }
    
}
```
Again, for simplicity sake, `_default_no_enc` is used, but `_default_enc_file` or `_default_enc_str` is recommended, and the code remains largely and greatly unchanged, as mentioned above.
See [test.Test](src/test/Test.java) which implements encryption, using `_default_enc_file` 
