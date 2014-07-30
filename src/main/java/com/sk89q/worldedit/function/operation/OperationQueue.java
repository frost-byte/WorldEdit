/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.util.task.progress.Progress;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Executes multiple queues in order.
 */
public class OperationQueue extends AbstractOperation {

    private final Deque<Operation> queue = new ArrayDeque<Operation>();
    private Operation current;

    /**
     * Create a new queue containing no operations.
     */
    public OperationQueue() {
    }

    /**
     * Create a new queue with operations from the given collection.
     *
     * @param operations a collection of operations
     */
    public OperationQueue(Collection<Operation> operations) {
        checkNotNull(operations);
        for (Operation operation : operations) {
            offer(operation);
        }
    }

    /**
     * Create a new queue with operations from the given array.
     *
     * @param operation an array of operations
     */
    public OperationQueue(Operation... operation) {
        this(Arrays.asList(checkNotNull(operation)));
    }

    @Override
    public boolean isOpportunistic() {
        if (!queue.isEmpty()) {
            for (Operation operation : queue) {
                if (!operation.isOpportunistic()) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a new operation to the queue.
     *
     * @param operation the operation
     */
    public void offer(Operation operation) {
        checkNotNull(operation);
        queue.offer(operation);
    }

    @Override
    public Result resume(RunContext run) throws Exception {
        if (current == null && !queue.isEmpty()) {
            current = queue.poll();
        }

        if (current != null) {
            Result currentResult = current.resume(run);

            if (currentResult == Result.STOP) {
                current = queue.poll();
            }
        }

        return current != null ? Result.CONTINUE : Result.STOP;
    }

    @Override
    public Progress getProgress() {
        return Progress.splitObservables(queue);
    }

}
