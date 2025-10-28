package com.empuje.redongs.web;

import com.empuje.redongs.dto.OrganizationDTO;
import com.empuje.redongs.dto.PresidentDTO;
import com.empuje.redongs.dto.RedOngsResult;
import com.empuje.redongs.soap.RedOngsSoapClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui/red-ongs")
public class RedOngsController {

    private final RedOngsSoapClient soap;

    public RedOngsController(RedOngsSoapClient soap) {
        this.soap = soap;
    }

    @GetMapping
    public String form(Model model, @RequestParam(value = "ids", required = false) String idsRaw) {
        // Render inicial (o permite compartir el link con ?ids=6,5,8,10)
        model.addAttribute("idsRaw", idsRaw == null ? "" : idsRaw);
        model.addAttribute("result", null);
        return "redongs/red_ongs"; // templates/redongs/red_ongs.html
    }

    @PostMapping
    public String submit(@RequestParam("ids") String idsRaw, Model model) {
        List<String> ids = parseIds(idsRaw);

        List<OrganizationDTO> orgs = soap.listAssociations(ids);
        List<PresidentDTO> presidents = soap.listPresidents(ids);

        model.addAttribute("idsRaw", idsRaw);
        model.addAttribute("result", new RedOngsResult(orgs, presidents));
        return "redongs/red_ongs";
    }

    // opcional: endpoint JSON si después se quiere usar fetch/AJAX
    @PostMapping(path = "/api", produces = "application/json")
    @ResponseBody
    public RedOngsResult submitApi(@RequestBody IdsPayload payload) {
        List<String> ids = parseIds(payload == null ? "" : payload.getIds());
        return new RedOngsResult(soap.listAssociations(ids), soap.listPresidents(ids));
    }

    private static List<String> parseIds(String raw) {
        if (!StringUtils.hasText(raw)) return List.of();
        return Arrays.stream(raw.split("[^0-9]+"))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public static class IdsPayload {
        private String ids;
        public String getIds() { return ids; }
        public void setIds(String ids) { this.ids = ids; }
    }
}
