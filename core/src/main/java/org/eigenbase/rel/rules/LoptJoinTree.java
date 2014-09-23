/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigenbase.rel.rules;

import java.util.*;

import org.eigenbase.rel.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Utility class used to store a {@link JoinRelBase} tree and the factors that
 * make up the tree.
 *
 * <p>Because {@link RelNode}s can be duplicated in a query
 * when you have a self-join, factor ids are needed to distinguish between the
 * different join inputs that correspond to identical tables. The class
 * associates factor ids with a join tree, matching the order of the factor ids
 * with the order of those factors in the join tree.
 */
public class LoptJoinTree {
  //~ Instance fields --------------------------------------------------------

  private final BinaryTree factorTree;
  private final RelNode joinTree;
  private final boolean removableSelfJoin;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a join-tree consisting of a single node.
   *
   * @param joinTree RelNode corresponding to the single node
   * @param factorId factor id of the node
   */
  public LoptJoinTree(RelNode joinTree, int factorId) {
    this.joinTree = joinTree;
    this.factorTree = new Leaf(factorId, this);
    this.removableSelfJoin = false;
  }

  /**
   * Associates the factor ids with a join-tree.
   *
   * @param joinTree RelNodes corresponding to the join tree
   * @param factorTree tree of the factor ids
   * @param removableSelfJoin whether the join corresponds to a removable
   * self-join
   */
  public LoptJoinTree(
      RelNode joinTree,
      BinaryTree factorTree,
      boolean removableSelfJoin) {
    this.joinTree = joinTree;
    this.factorTree = factorTree;
    this.removableSelfJoin = removableSelfJoin;
  }

  /**
   * Associates the factor ids with a join-tree given the factors corresponding
   * to the left and right subtrees of the join.
   *
   * @param joinTree RelNodes corresponding to the join tree
   * @param leftFactorTree tree of the factor ids for left subtree
   * @param rightFactorTree tree of the factor ids for the right subtree
   */
  public LoptJoinTree(
      RelNode joinTree,
      BinaryTree leftFactorTree,
      BinaryTree rightFactorTree) {
    this(joinTree, leftFactorTree, rightFactorTree, false);
  }

  /**
   * Associates the factor ids with a join-tree given the factors corresponding
   * to the left and right subtrees of the join. Also indicates whether the
   * join is a removable self-join.
   *
   * @param joinTree RelNodes corresponding to the join tree
   * @param leftFactorTree tree of the factor ids for left subtree
   * @param rightFactorTree tree of the factor ids for the right subtree
   * @param removableSelfJoin true if the join is a removable self-join
   */
  public LoptJoinTree(
      RelNode joinTree,
      BinaryTree leftFactorTree,
      BinaryTree rightFactorTree,
      boolean removableSelfJoin) {
    factorTree = new Node(leftFactorTree, rightFactorTree, this);
    this.joinTree = joinTree;
    this.removableSelfJoin = removableSelfJoin;
  }

  //~ Methods ----------------------------------------------------------------

  public RelNode getJoinTree() {
    return joinTree;
  }

  public LoptJoinTree getLeft() {
    final Node node = (Node) factorTree;
    return new LoptJoinTree(
        ((JoinRelBase) joinTree).getLeft(),
        node.getLeft(),
        node.getLeft().getParent().isRemovableSelfJoin());
  }

  public LoptJoinTree getRight() {
    final Node node = (Node) factorTree;
    return new LoptJoinTree(
        ((JoinRelBase) joinTree).getRight(),
        node.getRight(),
        node.getRight().getParent().isRemovableSelfJoin());
  }

  public BinaryTree getFactorTree() {
    return factorTree;
  }

  public List<Integer> getTreeOrder() {
    List<Integer> treeOrder = Lists.newArrayList();
    getTreeOrder(treeOrder);
    return treeOrder;
  }

  public void getTreeOrder(List<Integer> treeOrder) {
    factorTree.getTreeOrder(treeOrder);
  }

  public boolean isRemovableSelfJoin() {
    return removableSelfJoin;
  }

  //~ Inner Classes ----------------------------------------------------------

  /**
   * Simple binary tree class that stores an id in the leaf nodes and keeps
   * track of the parent LoptJoinTree object associated with the binary tree.
   */
  protected abstract static class BinaryTree {
    private final LoptJoinTree parent;

    protected BinaryTree(LoptJoinTree parent) {
      this.parent = Preconditions.checkNotNull(parent);
    }

    public LoptJoinTree getParent() {
      return parent;
    }

    public abstract void getTreeOrder(List<Integer> treeOrder);
  }

  /** Binary tree node that has no children. */
  protected static class Leaf extends BinaryTree {
    private final int id;

    public Leaf(int rootId, LoptJoinTree parent) {
      super(parent);
      this.id = rootId;
    }

    /**
     * @return the id associated with a leaf node in a binary tree
     */
    public int getId() {
      return id;
    }

    public void getTreeOrder(List<Integer> treeOrder) {
      treeOrder.add(id);
    }
  }

  /** Binary tree node that has two children. */
  protected static class Node extends BinaryTree {
    private BinaryTree left;
    private BinaryTree right;

    public Node(BinaryTree left, BinaryTree right, LoptJoinTree parent) {
      super(parent);
      this.left = Preconditions.checkNotNull(left);
      this.right = Preconditions.checkNotNull(right);
    }

    public BinaryTree getLeft() {
      return left;
    }

    public BinaryTree getRight() {
      return right;
    }

    public void getTreeOrder(List<Integer> treeOrder) {
      left.getTreeOrder(treeOrder);
      right.getTreeOrder(treeOrder);
    }
  }
}

// End LoptJoinTree.java
