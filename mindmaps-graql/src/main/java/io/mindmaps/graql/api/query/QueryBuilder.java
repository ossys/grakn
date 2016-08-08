/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package io.mindmaps.graql.api.query;

import com.google.common.collect.ImmutableSet;
import io.mindmaps.core.MindmapsTransaction;
import io.mindmaps.graql.internal.AdminConverter;
import io.mindmaps.graql.internal.query.InsertQueryImpl;
import io.mindmaps.graql.internal.query.MatchQueryImpl;
import io.mindmaps.graql.internal.query.VarImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * A starting point for creating queries.
 * <p>
 * A {@code QueryBuiler} is constructed with a {@code MindmapsTransaction}. All operations are performed using this
 * transaction. The user must explicitly commit or rollback changes after executing queries.
 * <p>
 * {@code QueryBuilder} also provides static methods for creating {@code Vars}.
 */
public class QueryBuilder {

    private final MindmapsTransaction transaction;

    private QueryBuilder(MindmapsTransaction transaction) {
        this.transaction = transaction;
    }

    /**
     * @param transaction  the transaction to operate the query on
     * @return a query builder using the specified transaction
     */
    public static QueryBuilder build(MindmapsTransaction transaction) {
        return new QueryBuilder(transaction);
    }

    /**
     * @return a query builder without a transaction to operate on
     */
    public static QueryBuilder build() {
        return new QueryBuilder(null);
    }

    /**
     * @param patterns an array of patterns to match in the graph
     * @return a match query that will find matches of the given patterns
     */
    public MatchQuery match(Pattern... patterns) {
        return match(Arrays.asList(patterns));
    }

    /**
     * @param patterns a collection of patterns to match in the graph
     * @return a match query that will find matches of the given patterns
     */
    public MatchQuery match(Collection<? extends Pattern> patterns) {
        return new MatchQueryImpl(Pattern.Admin.conjunction(AdminConverter.getPatternAdmins(patterns)), transaction);
    }

    /**
     * @param vars an array of variables to insert into the graph
     * @return an insert query that will insert the given variables into the graph
     */
    public InsertQuery insert(Var... vars) {
        return insert(Arrays.asList(vars));
    }

    /**
     * @param vars a collection of variables to insert into the graph
     * @return an insert query that will insert the given variables into the graph
     */
    public InsertQuery insert(Collection<? extends Var> vars) {
        ImmutableSet<Var.Admin> varAdmins = ImmutableSet.copyOf(AdminConverter.getVarAdmins(vars));
        return new InsertQueryImpl(varAdmins, Optional.empty(), Optional.ofNullable(transaction));
    }

    /**
     * @param name the name of the variable
     * @return a new query variable
     */
    public static Var var(String name) {
        return new VarImpl(Objects.requireNonNull(name));
    }

    /**
     * @return a new, anonymous query variable
     */
    public static Var var() {
        return new VarImpl();
    }

    /**
     * @param id the id of a concept
     * @return a query variable that identifies a concept by id
     */
    public static Var id(String id) {
        return var().id(id);
    }

    /**
     * @param patterns an array of patterns to match
     * @return a pattern that will match only when all contained patterns match
     */
    public static Pattern and(Pattern... patterns) {
        return and(Arrays.asList(patterns));
    }

    /**
     * @param patterns a collection of patterns to match
     * @return a pattern that will match only when all contained patterns match
     */
    public static Pattern and(Collection<? extends Pattern> patterns) {
        Pattern.Conjunction<Pattern.Admin> conjunction =
                Pattern.Admin.conjunction(AdminConverter.getPatternAdmins(patterns));

        return () -> conjunction;
    }

    /**
     * @param patterns an array of patterns to match
     * @return a pattern that will match when any contained pattern matches
     */
    public static Pattern or(Pattern... patterns) {
        return or(Arrays.asList(patterns));
    }

    /**
     * @param patterns a collection of patterns to match
     * @return a pattern that will match when any contained pattern matches
     */
    public static Pattern or(Collection<? extends Pattern> patterns) {
        Pattern.Disjunction<Pattern.Admin> disjunction =
                Pattern.Admin.disjunction(AdminConverter.getPatternAdmins(patterns));

        return () -> disjunction;
    }
}