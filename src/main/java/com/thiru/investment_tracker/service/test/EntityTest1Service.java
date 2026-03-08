package com.thiru.investment_tracker.service.test;

import com.thiru.investment_tracker.dto.test.ChangeDTO;
import com.thiru.investment_tracker.dto.test.JsonDiffPayload;
import com.thiru.investment_tracker.dto.test.RecordEntityTest1;
import com.thiru.investment_tracker.entity.test.EntityTest1;
import com.thiru.investment_tracker.entity.test.EntityTest2;
import com.thiru.investment_tracker.repository.test.EntityTest1Repository;
import com.thiru.investment_tracker.repository.test.EntityTest2Repository;
import lombok.RequiredArgsConstructor;
import org.javers.core.Javers;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntityTest1Service {
    private final EntityTest1Repository entityTest1Repository;
    private final EntityTest2Repository entityTest2Repository;

    private final Javers javers;

    public EntityTest1 addEntity1(RecordEntityTest1 entityTest) {
        EntityTest2 entityTest2 = new EntityTest2();
        entityTest2.setEmail(entityTest.email());
        entityTest2.setTransactionDate(entityTest.transactionDate());
        entityTest2Repository.save(entityTest2);



        EntityTest1 entityTest1 = new EntityTest1();
        entityTest1.setEmail(entityTest.email());
        entityTest1.setTransactionDate(entityTest.transactionDate());
        return entityTest1Repository.save(entityTest1);
    }

    public List<EntityTest1> getAllEntity1s() {
        return entityTest1Repository.findAll();
    }

    public EntityTest1 updateEntity1(RecordEntityTest1 entityTest) {
        EntityTest1 entityTest1 = new EntityTest1();
        entityTest1.setId(entityTest.id());
        entityTest1.setEmail(entityTest.email());
        entityTest1.setTransactionDate(entityTest.transactionDate());
        return entityTest1Repository.save(entityTest1);
    }

    public void deleteEntity1(String id) {
        entityTest1Repository.deleteById(id);
    }

    public EntityTest1 getEntity1ById(String id) {
        return entityTest1Repository.findById(id).orElse(null);
    }



//    public List<ValueChange> getDiff(String id, long v1, long v2) {
//
//        var q1 = QueryBuilder
//                .byInstanceId(id, EntityTest1.class)
//                .withVersion(v1)
//                .build();
//
//        var q2 = QueryBuilder
//                .byInstanceId(id, EntityTest1.class)
//                .withVersion(v2)
//                .build();
//
//        CdoSnapshot s1 = javers.findSnapshots(q1)
//                .stream().findFirst().orElseThrow();
//
//        CdoSnapshot s2 = javers.findSnapshots(q2)
//                .stream().findFirst().orElseThrow();
//
//        Diff diff = javers.compare(s1, s2);
//
//        return diff.getChangesByType(ValueChange.class);
//    }

    public List<CdoSnapshot> getEntityHistory(String entityId) {
        // Querying Javers for snapshots of a specific entity instance
        var jqlQuery = QueryBuilder.byInstanceId(entityId, EntityTest1.class)
                .withChildValueObjects() // Important for your AuditMetadata
                .limit(20) // Get the last 20 changes
                .build();

        return javers.findSnapshots(jqlQuery);
    }

    public List<ValueChange> getDiff(String id, long v1, long v2) {

        JqlQuery q1 = QueryBuilder
                .byInstanceId(id, EntityTest1.class)
                .withVersion(v1)
                .build();

        JqlQuery q2 = QueryBuilder
                .byInstanceId(id, EntityTest1.class)
                .withVersion(v2)
                .build();

        Shadow<Object> s1 = javers.findShadows(q1)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Version " + v1 + " not found"));

        Shadow<Object> s2 = javers.findShadows(q2)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Version " + v2 + " not found"));

        Object left  = s1.get();
        Object right = s2.get();

        Diff diff = javers.compare(left, right);

        return diff.getChangesByType(ValueChange.class);
    }

    public JsonDiffPayload getChangeDTOs(String id, long v1, long v2) {

        var q1 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v1).build();
        var q2 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v2).build();

        var leftObj  = javers.findShadows(q1).stream().findFirst().orElseThrow().get();
        var rightObj = javers.findShadows(q2).stream().findFirst().orElseThrow().get();
        return new JsonDiffPayload(leftObj, rightObj);
    }




//    public List<ValueChange> getDiff(String id, long v1, long v2) {
//
//        QueryBuilder query = QueryBuilder
//                .byInstanceId(id, EntityTest1.class)
//                .from(v1)
//                .to(v2);
//
//        Diff diff = javers.findChanges(query);
//
//        return diff.getChangesByType(ValueChange.class);
//    }

}
