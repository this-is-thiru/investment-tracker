package com.thiru.investment_tracker.controller.test;

import com.thiru.investment_tracker.dto.test.ChangeDTO;
import com.thiru.investment_tracker.dto.test.JsonDiffPayload;
import com.thiru.investment_tracker.dto.test.RecordEntityTest1;
import com.thiru.investment_tracker.entity.test.EntityTest1;
import com.thiru.investment_tracker.service.test.EntityTest1Service;
import lombok.RequiredArgsConstructor;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/entity-test1")
@RequiredArgsConstructor
public class EntityTest1Controller {
    private final EntityTest1Service entityTest1Service;

    @PostMapping("/add")
    public EntityTest1 addEntity1(@RequestBody RecordEntityTest1 entityTest1) {
        return entityTest1Service.addEntity1(entityTest1);
    }

    @PutMapping("/update")
    public EntityTest1 updateEntity1(@RequestBody RecordEntityTest1 entityTest1) {
        return entityTest1Service.updateEntity1(entityTest1);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteEntity1(@PathVariable String id) {
        entityTest1Service.deleteEntity1(id);
    }

    @GetMapping("/get/{id}/diff/{v1}/{v2}")
    public JsonDiffPayload getDiff(@PathVariable String id, @PathVariable long v1, @PathVariable long v2) {
        return entityTest1Service.getChangeDTOs(id, v1, v2);
    }

    @GetMapping("/{entityId}")
    public List<CdoSnapshot> getEntityHistory(@PathVariable String entityId) {
       return entityTest1Service.getEntityHistory(entityId);
    }

}
