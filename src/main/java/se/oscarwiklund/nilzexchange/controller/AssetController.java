package se.oscarwiklund.nilzexchange.controller;

import org.springframework.web.bind.annotation.*;
import se.oscarwiklund.nilzexchange.model.Asset;
import se.oscarwiklund.nilzexchange.repository.AssetRepository;

import java.util.List;

@RestController
@RequestMapping("/asset")
public class AssetController {

    private final AssetRepository repo;

    public AssetController(AssetRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Asset create(@RequestBody Asset asset) {
        return repo.save(asset);
    }

    @GetMapping
    public List<Asset> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{symbol}")
    public Asset getBySymbol(@PathVariable String symbol) {
        return repo.findBySymbol(symbol);
    }
}
