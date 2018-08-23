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

import com.sk89q.worldedit.WorldEditException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AffectedFutureOperation implements Operation {

    private Operation operation;
    private CompletableFuture<Integer> future;
    private int affected = 0;

    public AffectedFutureOperation(Operation operation, CompletableFuture<Integer> future) {
        this.operation = operation;
        this.future = future;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        if (operation != null) {
            Operation nextOp = operation.resume(run);
            if (operation instanceof AffectingOperation) {
                affected += ((AffectingOperation) operation).getAffected();
            }
            operation = nextOp;
        }

        if (operation != null) {
            return this;
        }

        future.complete(affected);
        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addStatusMessages(List<String> messages) {
        messages.add("Affected " + affected);
        operation.addStatusMessages(messages);
    }
}
