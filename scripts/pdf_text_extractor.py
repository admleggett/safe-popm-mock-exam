#!/usr/bin/env python3
"""
PDF Text Extractor - Extract text content from PDF files
"""

import argparse
import sys
from pathlib import Path

try:
    import PyPDF2
except ImportError:
    print("PyPDF2 library not found. Installing...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "PyPDF2"])
    import PyPDF2


def extract_text_from_pdf(pdf_path, output_file=None):
    """
    Extract text from a PDF file.
    
    Args:
        pdf_path (str): Path to the PDF file
        output_file (str, optional): Path to save the extracted text
        
    Returns:
        str: Extracted text content
    """
    try:
        with open(pdf_path, 'rb') as file:
            reader = PyPDF2.PdfReader(file)
            text = []
            
            for page_num in range(len(reader.pages)):
                page = reader.pages[page_num]
                text.append(page.extract_text())
            
            full_text = "\n\n".join(text)
            
            if output_file:
                with open(output_file, 'w', encoding='utf-8') as out_file:
                    out_file.write(full_text)
                print(f"Text extracted and saved to {output_file}")
            
            return full_text
    except FileNotFoundError:
        print(f"Error: File '{pdf_path}' not found.")
        return None
    except Exception as e:
        print(f"Error extracting text: {e}")
        return None


def main():
    parser = argparse.ArgumentParser(description='Extract text from PDF files')
    parser.add_argument('pdf_path', help='Path to the PDF file')
    parser.add_argument('-o', '--output', help='Path to save the extracted text (optional)')
    args = parser.parse_args()
    
    pdf_path = args.pdf_path
    output_file = args.output
    
    if not Path(pdf_path).exists():
        print(f"Error: File '{pdf_path}' does not exist.")
        sys.exit(1)
    
    if output_file is None:
        # If no output file is specified, print to stdout
        text = extract_text_from_pdf(pdf_path)
        if text:
            print(text)
    else:
        extract_text_from_pdf(pdf_path, output_file)


if __name__ == "__main__":
    main()
