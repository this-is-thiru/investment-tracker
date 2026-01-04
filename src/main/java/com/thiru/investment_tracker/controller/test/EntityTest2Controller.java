package com.thiru.investment_tracker.controller.test;

import com.thiru.investment_tracker.dto.test.ChangeDTO;
import com.thiru.investment_tracker.dto.test.RecordEntityTest1;
import com.thiru.investment_tracker.entity.test.EntityTest1;
import com.thiru.investment_tracker.service.test.EntityTest1Service;
import lombok.RequiredArgsConstructor;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Controller
@RequestMapping("/entity-test1")
@RequiredArgsConstructor
public class EntityTest2Controller {
    private final EntityTest1Service entityTest1Service;
    private final ObjectMapper mapper;

    private final Javers javers;

//    @GetMapping("/get/{id}/diff/{v1}/{v2}")
//    public String getDiff(@PathVariable String id, @PathVariable long v1, @PathVariable long v2, Model model) {
//        List<ChangeDTO> changes = entityTest1Service.getChangeDTOs(id, v1, v2);
//
//        model.addAttribute("id", id);
//        model.addAttribute("v1", v1);
//        model.addAttribute("v2", v2);
//        model.addAttribute("changes", changes);
//
//        return "diff";  // resolves to templates/diff.html
//    }

//    @GetMapping("/get/{id}/diff/{v1}/{v2}")
//    public String showJsonDiff(
//            @PathVariable String id, @PathVariable long v1, @PathVariable long v2, Model model) {
//
//
//        var q1 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v1).build();
//        var q2 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v2).build();
//
//        var leftObj = javers.findShadows(q1).stream().findFirst().orElseThrow().get();
//        var rightObj = javers.findShadows(q2).stream().findFirst().orElseThrow().get();
//
//        model.addAttribute("leftJson", mapper.writeValueAsString(leftObj));
//        model.addAttribute("rightJson", mapper.writeValueAsString(rightObj));
//
//        return "jsondiff";
//    }
//
//    @GetMapping("/get/{id}/diff/{v1}/{v2}")
//    public String showJsonDiff(
//            @PathVariable String id, @PathVariable long v1, @PathVariable long v2, Model model) {
//
//
//        var q1 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v1).build();
//        var q2 = QueryBuilder.byInstanceId(id, EntityTest1.class).withVersion(v2).build();
//
//        var leftObj = javers.findShadows(q1).stream().findFirst().orElseThrow().get();
//        var rightObj = javers.findShadows(q2).stream().findFirst().orElseThrow().get();
//
//        model.addAttribute("leftJson", mapper.writeValueAsString(leftObj));
//        model.addAttribute("rightJson", mapper.writeValueAsString(rightObj));
//
//        return "jsondiff";
//    }

}
