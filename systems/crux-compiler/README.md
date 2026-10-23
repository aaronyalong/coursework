# Crux Compiler  

## 📜 Overview  
This project is a **compiler for the Crux programming language**, implemented in **Java 11** using **ANTLR**. It follows a multi-stage design that progressively transforms source code into **x86 assembly**.  

## 🏗️ Compiler Stages  
The compiler consists of the following stages:  
1. **Parsing** – Uses ANTLR to generate a parse tree from Crux source code.  
2. **Abstract Syntax Tree (AST)** – Converts the parse tree into an AST for semantic analysis.  
3. **Semantic Analysis** – Type checking and validation of the Crux source program.  
4. **Intermediate Representation (IR)** – Generates an intermediate code structure for further transformation.  
5. **Code Generation** – Produces executable x86 assembly code from the IR.  

## 🚀 Features  
- Written in **Java 11** with **ANTLR** for parsing  
- Uses **Maven** for dependency management and building  
- Supports **unit testing** to verify each stage of compilation  
- Generates **x86 assembly output**  

## 🛠️ Installation & Setup  

### 1️⃣ Clone the Repository  
```sh
git clone https://github.com/YOUR_GITHUB_USERNAME/Crux-Compiler.git
cd Crux-Compiler

We, Aaron Yalong and Sharbel Ghostine, hereby certify that the files we submitted represent our own work, that we did not copy any code from any other person or source, and that we did not share our code with any other students.

# Crux-Language-Processor-and-Compiler
