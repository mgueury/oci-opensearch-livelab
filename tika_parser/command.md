fn --verbose deploy --app mg-app
echo -n '{"bucketName": "mg-input-bucket", "namespace": "frsxwtjslf35", "resourceName": "excell_2007.xlsx"}' | fn invoke mg-app tikaparser

{
    "date": "2013-04-08T11:37:06Z",
    "extended-properties:AppVersion": "12.0000",
    "dc:creator": "mgueury",
    "extended-properties:Company": "Oracle Corporation",
    "dcterms:created": "2013-04-08T11:35:59Z",
    "dcterms:modified": "2013-04-08T11:37:06Z",
    "Last-Modified": "2013-04-08T11:37:06Z",
    "Last-Save-Date": "2013-04-08T11:37:06Z",
    "protected": "false",
    "meta:save-date": "2013-04-08T11:37:06Z",
    "Application-Name": "Microsoft Excel",
    "modified": "2013-04-08T11:37:06Z",
    "Content-Type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "X-Parsed-By": "org.apache.tika.parser.DefaultParser",
    "creator": "mgueury",
    "meta:author": "mgueury",
    "meta:creation-date": "2013-04-08T11:35:59Z",
    "extended-properties:Application": "Microsoft Excel",
    "meta:last-author": "mgueury",
    "Creation-Date": "2013-04-08T11:35:59Z",
    "Last-Author": "mgueury",
    "Application-Version": "12.0000",
    "extended-properties:DocSecurityString": "None",
    "Author": "mgueury",
    "publisher": "Oracle Corporation",
    "dc:publisher": "Oracle Corporation",
    "content": "Sheet1\tTest XLSX file\t1\t1Sheet2Sheet3"
}


