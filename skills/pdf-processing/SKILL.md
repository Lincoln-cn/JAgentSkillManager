---
name: pdf-processing
description: Extract text and tables from PDF files, fill forms, merge documents. Use when working with PDF documents or when user mentions PDFs, forms, or document extraction.
license: Apache-2.0
metadata:
  author: agent-skill-team
  version: "1.0"
---

# PDF Processing Skill

## When to use this skill
Use this skill when the user needs to work with PDF files, extract text content, fill PDF forms, or merge multiple PDF documents. This skill handles various PDF operations including text extraction, table extraction, form filling, and document merging.

## How to extract text from PDF

To extract text content from a PDF file:

1. **Input**: Provide the PDF file path or upload the PDF file
2. **Process**: The skill will use PDF processing library to extract all text content
3. **Output**: Returns extracted text with page numbers and formatting information

### Parameters
- `file_path` (required): Path to the PDF file
- `preserve_formatting` (optional): Whether to preserve text formatting (default: true)
- `page_range` (optional): Specific page range to extract (e.g., "1-5")

### Example
```
Request: "Extract text from the contract.pdf"
Parameters: {
  "file_path": "/documents/contract.pdf",
  "preserve_formatting": true
}
```

## How to extract tables from PDF

To extract structured table data from PDFs:

1. **Identify**: Look for table structures in the PDF pages
2. **Extract**: Pull table data with row/column structure
3. **Format**: Return as structured data (JSON/CSV format)

### Parameters
- `file_path` (required): Path to the PDF file
- `table_format` (optional): Output format - "json" or "csv" (default: "json")
- `page_numbers` (optional): Specific pages to search for tables

### Example
```
Request: "Extract tables from the financial report"
Parameters: {
  "file_path": "/reports/financial.pdf",
  "table_format": "json",
  "page_numbers": "1-10"
}
```

## How to fill PDF forms

To fill interactive form fields in PDF documents:

1. **Load**: Load the PDF with form fields
2. **Identify**: Detect all fillable form fields
3. **Fill**: Apply provided data to corresponding fields
4. **Save**: Generate filled PDF output

### Parameters
- `file_path` (required): Path to the PDF form
- `form_data` (required): JSON object with field names and values
- `output_path` (optional): Path for filled PDF output

### Example
```
Request: "Fill out the application form with my information"
Parameters: {
  "file_path": "/forms/application.pdf",
  "form_data": {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123"
  },
  "output_path": "/forms/filled-application.pdf"
}
```

## How to merge PDFs

To combine multiple PDF documents into a single file:

1. **Collect**: Gather all PDF file paths
2. **Order**: Specify the order of merging
3. **Merge**: Combine into single PDF with bookmarks
4. **Output**: Generate merged PDF file

### Parameters
- `file_paths` (required): Array of PDF file paths to merge
- `output_path` (required): Path for merged PDF output
- `add_bookmarks` (optional): Whether to add bookmarks for each original file (default: true)

### Example
```
Request: "Merge all chapters into one document"
Parameters: {
  "file_paths": [
    "/documents/chapter1.pdf",
    "/documents/chapter2.pdf", 
    "/documents/chapter3.pdf"
  ],
  "output_path": "/documents/complete-book.pdf",
  "add_bookmarks": true
}
```

## Error Handling

Common errors and their solutions:

- **File not found**: Verify the file path is correct and file exists
- **Corrupted PDF**: The PDF may be damaged, try recovery tools
- **Password protected**: Provide password for encrypted PDFs
- **Memory issues**: Large PDFs may require chunked processing

## Scripts Reference

This skill can execute the following helper scripts:

### `scripts/extract-pdf.py`
Python script for advanced text extraction with OCR support for scanned PDFs.

### `scripts/merge-pdfs.py`
Python script for merging multiple PDFs with customizable options.

### `scripts/fill-form.py`
Python script for intelligent form field detection and filling.

## Performance Notes

- **Text extraction**: ~100MB PDFs process in <10 seconds
- **Table extraction**: Depends on table complexity, typically 5-30 seconds
- **Form filling**: Usually <5 seconds for standard forms
- **PDF merging**: Linear time based on total file size

For best performance, ensure adequate system memory and consider chunked processing for very large documents.