/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.gjt.sp.jedit.bsh.org.objectweb.asm;

/**
 * A visitor to visit a Java class. The methods of this interface must be called
 * in the following order: {@code visit} ({@code visitField} |
 * {@code visitMethod} | {@code visitInnerClass})* {@code visitEnd}.
 */

public interface ClassVisitor {

  /**
   * Visits the header of the class.
   *
   * @param access the class's access flags (see {@link Constants}). This
   *      parameter also indicates if the class is deprecated.
   * @param name the internal name of the class (see {@link Type#getInternalName
   *      getInternalName}).
   * @param superName the internal of name of the super class (see {@link
   *      Type#getInternalName getInternalName}). For interfaces, the super
   *      class is {@link Object}. May be {@code null}, but only for the {@link
   *      Object java.lang.Object} class.
   * @param interfaces the internal names of the class's interfaces (see {@link
   *      Type#getInternalName getInternalName}). May be {@code null}.
   * @param sourceFile the name of the source file from which this class was
   *      compiled. May be {@code null}.
   */

  void visit (
    int access,
    String name,
    String superName,
    String[] interfaces,
    String sourceFile);

  /**
   * Visits information about an inner class. This inner class is not
   * necessarily a member of the class being visited.
   *
   * @param name the internal name of an inner class (see {@link
   *      Type#getInternalName getInternalName}).
   * @param outerName the internal name of the class to which the inner class
   *      belongs (see {@link Type#getInternalName getInternalName}). May be
   *      {@code null}.
   * @param innerName the (simple) name of the inner class inside its enclosing
   *      class. May be {@code null} for anonymous inner classes.
   * @param access the access flags of the inner class as originally declared
   *      in the enclosing class.
   */

  void visitInnerClass (
    String name,
    String outerName,
    String innerName,
    int access);

  /**
   * Visits a field of the class.
   *
   * @param access the field's access flags (see {@link Constants}). This
   *      parameter also indicates if the field is synthetic and/or deprecated.
   * @param name the field's name.
   * @param desc the field's descriptor (see {@link Type Type}).
   * @param value the field's initial value. This parameter, which may be
   *      {@code null} if the field does not have an initial value, must be an
   *      {@link java.lang.Integer Integer}, a {@link java.lang.Float Float}, a
   *      {@link java.lang.Long Long}, a {@link java.lang.Double Double} or a
   *      {@link String String}.
   */

  void visitField (int access, String name, String desc, Object value);

  /**
   * Visits a method of the class. This method <i>must</i> return a new
   * {@link CodeVisitor CodeVisitor} instance (or {@code null}) each time it
   * is called, i.e., it should not return a previously returned visitor.
   *
   * @param access the method's access flags (see {@link Constants}). This
   *      parameter also indicates if the method is synthetic and/or deprecated.
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link Type Type}).
   * @param exceptions the internal names of the method's exception
   *      classes (see {@link Type#getInternalName getInternalName}). May be
   *      {@code null}.
   * @return an object to visit the byte code of the method, or {@code null} if
   *      this class visitor is not interested in visiting the code of this
   *      method.
   */

  CodeVisitor visitMethod (
    int access,
    String name,
    String desc,
    String[] exceptions);

  /**
   * Visits the end of the class. This method, which is the last one to be
   * called, is used to inform the visitor that all the fields and methods of
   * the class have been visited.
   */

  void visitEnd ();
}
