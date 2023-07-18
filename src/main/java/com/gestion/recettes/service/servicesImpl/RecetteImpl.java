package com.gestion.recettes.service.servicesImpl;


import com.gestion.recettes.dto.*;
import com.gestion.recettes.entities.*;
import com.gestion.recettes.repos.CategorieRepo;
import com.gestion.recettes.repos.IngredientRepo;
import com.gestion.recettes.repos.PersonneRepo;
import com.gestion.recettes.repos.RecetteRepo;
import com.gestion.recettes.service.RecetteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gestion.recettes.service.servicesImpl.BesoinImpl.*;
import static com.gestion.recettes.service.servicesImpl.CategorieImpl.convertToCategorieDTO;
import static com.gestion.recettes.service.servicesImpl.CategorieImpl.convertToCategorieList;
import static com.gestion.recettes.service.servicesImpl.EtapeImpl.*;
import static com.gestion.recettes.service.servicesImpl.IngredientImpl.*;
import static com.gestion.recettes.service.servicesImpl.MediaImpl.*;
import static com.gestion.recettes.service.servicesImpl.MotCleImpl.convertToMotCleDto;
import static com.gestion.recettes.service.servicesImpl.MotCleImpl.convertToMotCleList;
import static com.gestion.recettes.service.servicesImpl.PersonneImpl.convertToPersonne;
import static com.gestion.recettes.service.servicesImpl.PersonneImpl.convertToPersonneDTO;
import static com.gestion.recettes.service.servicesImpl.QuantiteImpl.*;

@Service
public class RecetteImpl implements RecetteService {

    private static RecetteRepo recetteRepoStat;
    private final RecetteRepo recetteRepo;
    private final IngredientRepo ingredientRepo;
    private final IngredientImpl ingredientImpl;
    private final QuantiteImpl quantiteImpl;
    private final CategorieImpl categorieImpl;
    private final EtapeImpl etapeImpl;
    private final MotCleImpl motCleImpl;
    private final BesoinImpl besoinImpl;
    private final MediaImpl mediaImpl;
    private final PersonneRepo personneRepo;
    private final JdbcTemplate jdbcTemplate;
    private final CategorieRepo categorieRepo;

    @Autowired
    public RecetteImpl(RecetteRepo recetteRepo, IngredientRepo ingredientRepo, IngredientImpl ingredientImpl,
                       QuantiteImpl quantiteImpl, CategorieImpl categorieImpl, EtapeImpl etapeImpl,
                       MotCleImpl motCleImpl, BesoinImpl besoinImpl, MediaImpl mediaImpl,
                       PersonneRepo personneRepo, JdbcTemplate jdbcTemplate, CategorieRepo categorieRepo) {
        this.recetteRepo = recetteRepo;
        this.ingredientRepo = ingredientRepo;
        this.ingredientImpl = ingredientImpl;
        this.quantiteImpl = quantiteImpl;
        this.categorieImpl = categorieImpl;
        this.etapeImpl = etapeImpl;
        this.motCleImpl = motCleImpl;
        this.besoinImpl = besoinImpl;
        this.mediaImpl = mediaImpl;
        this.personneRepo = personneRepo;
        this.jdbcTemplate = jdbcTemplate;
        RecetteImpl.recetteRepoStat = recetteRepo;
        this.categorieRepo = categorieRepo;
    }

    //    @Override
//    public RecetteDto creer(RecetteDto recetteDto) {
//        Recette recette = convertToRecette(recetteDto);
//        recette.setIngredients(convertToIngredientList(recetteDto.getIngredients()));
////        recette.setCategories(convertToCategorieList(recetteDto.getCategories()));
////        recette.setEtapes(convertToEtapeList(recetteDto.getEtapes()));
////        recette.setBesoins();
//        recetteRepo.save(recette);
//        return convertToRecetteDto(recette);
//    }
    @Override
    public RecetteDto creer(RecetteDto recetteDto, HttpSession session) {
        Recette recette = convertToRecette(recetteDto);

        List<Ingredient> ingredients = convertToIngredientList(recetteDto.getIngredients());
        List<Categorie> categories = convertToCategorieList(recetteDto.getCategories());
        List<Besoin> besoins = convertToBesoinList(recetteDto.getBesoins());
        List<Media> medias = convertToMediaList(recetteDto.getMedias());
        List<MotCle> motCles = convertToMotCleList(recetteDto.getMotCles());
        List<Etape> etapes = convertToEtapeList(recetteDto.getEtapes());
        List<Quantite> quantites = convertToQuantiteList(recetteDto.getQuantites());

        List<Recette> recettesRef = new ArrayList<>();
        for (RecetteRefDto recetteRefDto : recetteDto.getRecettesRef()){
            Optional<Recette> optionalRecette = recetteRepo.findById(recetteRefDto.getId());
            if(optionalRecette.isPresent()) {
                Recette recetteAdd = optionalRecette.get();
                recettesRef.add(recetteAdd);
            }
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient.getId() == null) {
                IngredientDto createdIngredient = ingredientImpl.creer(convertToIngredientDTO(ingredient));
                ingredient.setId(createdIngredient.getId());
            }
        }

        List<QuantiteDto> quantiteDtos = new ArrayList<>();
        for (int i = 0; i < quantites.size(); i++) {
            Quantite quantite = quantites.get(i);
            Ingredient ingredient = ingredients.get(i);
            quantite.setIngredient(ingredient);
            quantiteDtos.add(quantiteImpl.creer(convertToQuantiteDto(quantite)));
        }

        for (Categorie categorie : categories) {
            if (categorie.getIdCat() == null){
                CategorieDto categorieDTO = categorieImpl.creer(convertToCategorieDTO(categorie));
                categorie.setIdCat(categorieDTO.getIdCat());
            }
        }
        for (Categorie categorie : categories) {
            if (categorie.getIdCat() != null) {
                // Fetch the existing category from the database using its ID
                Categorie existingCategorie = categorieRepo.findById(categorie.getIdCat()).orElse(null);
                if (existingCategorie != null) {
                    // Assign the existing category to the recette
                    categorie.setIdCat(existingCategorie.getIdCat());
                    // You may also need to update other properties of the category if necessary
                    // categorie.setName(existingCategorie.getName());
                    // categorie.setDescription(existingCategorie.getDescription());
                }
            }
        }

        List<EtapeDto> etapeDtos = new ArrayList<>();
        for (Etape etape : etapes) {
            if (etape.getId() == null){
                etapeDtos.add(etapeImpl.creer(convertToEtapeDTO(etape)));
            }
        }

        for (MotCle motCle : motCles) {
            if (motCle.getId() == null){
                MotCleDto motCleDto = motCleImpl.creer(convertToMotCleDto(motCle));
                motCle.setId(motCleDto.getId());
            }
        }

        for (Besoin besoin : besoins) {
            if (besoin.getId() == null){
                BesoinDto besoinDTO = besoinImpl.creer(convertToBesoinDTO(besoin));
                besoin.setId(besoinDTO.getId());
            }
        }

        List<MediaDto> mediaDtos = new ArrayList<>();
        for (Media media1 : medias) {
            MediaDto mediaDTO = mediaImpl.creer(convertToMediaDTO(media1));
            mediaDtos.add(mediaDTO);
        }

        recette.setVisibilitee("public");
        recette.setQuantites(quantites);
        recette.setIngredients(ingredients);
        recette.setCategories(categories);
        recette.setEtapes(etapes);
        recette.setMotCles(motCles);
        recette.setBesoins(besoins);
        recette.setMedias(medias);
        recette.setRecettes(recettesRef);
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null && personneRepo.existsById(userId)){
            Personne personneCre = personneRepo.getReferenceById(userId);
            recette.setUtilisateurCreateur(personneCre);
        }

        recette = recetteRepo.save(recette);

        for (QuantiteDto quantiteDto : quantiteDtos){
            quantiteDto.setRecetteId(recette.getId());
            quantiteImpl.modifier(quantiteDto.getId(), quantiteDto);
        }
        for (EtapeDto etapeDTO : etapeDtos){
            etapeDTO.setRecetteId(recette.getId());
            etapeImpl.modifier(etapeDTO.getId(), etapeDTO);
        }
        for (MediaDto mediaDTO : mediaDtos) {
            mediaDTO.setRecetteId(recette.getId());
            mediaImpl.modifier(mediaDTO.getId(), mediaDTO);
        }

        return convertToRecetteDto(recette);
    }



    @Override
    public RecetteDto lire(Long id) {
        Optional<Recette> optionalRecette = recetteRepo.findById(id);
        if (optionalRecette.isPresent()) {
            Recette recette = optionalRecette.get();
            RecetteDto recetteDto = convertToRecetteDto(recette);
            recetteDto.setCommentaires(commentairesRecette(id));
           return recetteDto;
        } else {
            System.out.println("Cette recette n'existe pas");
            return null;
        }
    }

    @Override
    public List<RecetteDto> lireTous() {
        List<Recette> getRecipes = recetteRepo.findAll();
        List<RecetteDto> recetteDtoList = new ArrayList<>();
        for (RecetteDto recetteDto : convertToRecetteDtoList(getRecipes)){
            recetteDto.setCommentaires(commentairesRecette(recetteDto.getId()));
            recetteDtoList.add(recetteDto);
        }
        return recetteDtoList;
    }

    @Override
    public RecetteDto modifier(Long id,RecetteDto recetteDto) {

        Optional<Recette> optionalRecette = recetteRepo.findById(id);
        if(optionalRecette.isPresent()){

            Recette recette = optionalRecette.get();

            if (recetteDto.getNom() != null) {
                recette.setNom(recetteDto.getNom());
            }
            if (recetteDto.getDescription() != null) {
                recette.setDescription(recetteDto.getDescription());
            }
            if (recetteDto.getOrigine() != null) {
                recette.setOrigine(recetteDto.getOrigine());
            }
            if ((Integer) recetteDto.getDureePreparation() != null) {
                recette.setDureePreparation(recetteDto.getDureePreparation());
            }
            if ((Integer) recetteDto.getDureeCuisson() != null) {
                recette.setDureeCuisson(recetteDto.getDureeCuisson());
            }
            if ((Integer) recetteDto.getNbrPersonnes() != null) {
                recette.setNbrPersonnes(recetteDto.getNbrPersonnes());
            }
            if (recetteDto.getVisibilitee() != null) {
                recette.setVisibilitee(recetteDto.getVisibilitee());
            }
            if ((Integer) recetteDto.getStatut() != null) {
                recette.setStatut(recetteDto.getStatut());
            }
            if (recetteDto.getDateSoumission() != null) {
                recette.setDateSoumission(recetteDto.getDateSoumission());
            }
            if (recetteDto.getDatePublication() != null) {
                recette.setDatePublication(recetteDto.getDatePublication());
            }
            if (recetteDto.getUtilisateurCreateur() != null) {
                recette.setUtilisateurCreateur(convertToPersonne(recetteDto.getUtilisateurCreateur()));
            }
            if (recetteDto.getUtilisateurApprobateur() != null) {
                recette.setUtilisateurApprobateur(convertToPersonne(recetteDto.getUtilisateurApprobateur()));
            }
            if (recetteDto.getSignalants() != null) {
                recette.setSignalants(PersonneImpl.convertToPersonneList(recetteDto.getSignalants()));
            }
            if(recetteDto.getMedias() != null) {
                List<Media> medias = new ArrayList<>();
                for (MediaDto mediaDTO : recetteDto.getMedias()){
                    medias.add(convertToMedia(mediaImpl.modifier(mediaDTO.getId(), mediaDTO)));
                }
                recette.setMedias(medias);
            }
            if(recetteDto.getBesoins() != null) {
                List<Besoin> besoins = new ArrayList<>();
                for (BesoinDto besoinDTO : recetteDto.getBesoins()){
                    besoins.add(convertToBesoin(besoinImpl.modifier(besoinDTO.getId(), besoinDTO)));
                }
                recette.setBesoins(besoins);
            }
            if(recetteDto.getMotCles() != null) {
                recette.setMotCles(convertToMotCleList(recetteDto.getMotCles()));
            }
            if(recetteDto.getIngredients() != null) {
                recette.setIngredients(convertToIngredientList(recetteDto.getIngredients()));
            }
            if(recetteDto.getEtapes() != null) {
                List<Etape> etapes = new ArrayList<>();
                for (EtapeDto etapeDTO : recetteDto.getEtapes()){
                    etapes.add(convertToEtape(etapeImpl.modifier(etapeDTO.getId(), etapeDTO)));
                }
                recette.setEtapes(etapes);
            }
            if(recetteDto.getCategories() != null) {
                recette.setCategories(convertToCategorieList(recetteDto.getCategories()));
            }
            if(recetteDto.getTypeRelation() != null) {
                recette.setTypeRelation(TypeRelationImpl.convertToTypeRelation(recetteDto.getTypeRelation()));
            }
            if(recetteDto.getTypeRel() != null) {
                recette.setTypeRel(TypeRelationImpl.convertToTypeRelation(recetteDto.getTypeRel()));
            }
            /*if(recetteDto.getRecettes() != null) {
                recette.setRecettes(RecetteImpl.convertToRecetteList(recetteDto.getRecettes()));
            }*/
            if (recetteDto.getRecettesRef() != null){
                List<Recette> recettesRef = new ArrayList<>();
                for (RecetteRefDto recetteRefDto : recetteDto.getRecettesRef()){
                    Optional<Recette> optionalRecette1 = recetteRepo.findById(recetteRefDto.getId());
                    if(optionalRecette.isPresent()) {
                        Recette recetteAdd = optionalRecette.get();
                        recettesRef.add(recetteAdd);
                    }
                }
                recette.setRecettes(recettesRef);
            }
            if(recetteDto.getQuantites() != null) {
                List<Quantite> quantites = new ArrayList<>();
                for (QuantiteDto quantiteDto : recetteDto.getQuantites()){
                    quantites.add(convertToQuantite(quantiteImpl.modifier(quantiteDto.getId(), quantiteDto)));
                }
                recette.setQuantites(quantites);
            }
            Recette updatedRecette = recetteRepo.save(recette);
            return convertToRecetteDto(updatedRecette);

        } else{
           System.out.println("Cette Recette n'existe pas");
        }
        return recetteDto;
    }

    @Override
    public Boolean supprimer(Long id) {
        if(recetteRepo.existsById(id))
        {
            recetteRepo.deleteById(id);
        }else{
            System.out.println("Cette recette n'existe pas");
        }
        return null;
    }

    @Override
    public List<RecetteDto> recettesByCategorie(Long id) {
        String sql = "SELECT r.* FROM recette r " +
                "JOIN recette_categories rc ON r.id = rc.recettes_id " +
                "WHERE rc.categories_id_cat = ?";

        return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
            // Extract data from the result set and create RecetteDto objects
            Long idRecette = rs.getLong("id");

            Optional<Recette> optionalRecette = recetteRepo.findById(idRecette);
            RecetteDto recetteDto = optionalRecette.map(RecetteImpl::convertToRecetteDto).orElse(null);

            return recetteDto;
        });
    }

    @Override
    public List<RecetteDto> searchRecettesByNom(String nom) {
        String sql = "SELECT * FROM recette WHERE nom LIKE ?";

        String searchValue = "%" + nom + "%";

        return jdbcTemplate.query(sql, new Object[]{searchValue}, (rs, rowNum) -> {
            Long idRecette = rs.getLong("id");

            Optional<Recette> optionalRecette = recetteRepo.findById(idRecette);
            RecetteDto recetteDto = optionalRecette.map(RecetteImpl::convertToRecetteDto).orElse(null);

            return recetteDto;
        });
    }

    @Override
    public List<RecetteDto> mesRecettes(Long id) {
        String sql = "SELECT * FROM recette WHERE utilisateur_createur_id = ?";

        return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
            Long idRecette = rs.getLong("id");

            Optional<Recette> optionalRecette = recetteRepo.findById(idRecette);
            RecetteDto recetteDto = optionalRecette.map(RecetteImpl::convertToRecetteDto).orElse(null);

            return recetteDto;
        });
    }

    @Override
    public List<RecetteDto> recettesSignalees() {
        String sql = "SELECT recettes_signalees_id FROM personne_recettes_signalees";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id_recette = rs.getLong("recettes_signalees_id");
            Optional<Recette> optionalRecette = recetteRepo.findById(id_recette);
            if (optionalRecette.isPresent()) {
                Recette recette = optionalRecette.get();
                return convertToRecetteDto(recette);
            } else {
                return null; // Or you can throw an exception or handle the missing recipe case differently
            }
        });
    }



    @Override
    public List<CommentaireDto> commentairesRecette(Long idRecette) {
        String sql = "SELECT * FROM commentaire WHERE recette_id = ?";

        return jdbcTemplate.query(sql, new Object[]{idRecette}, (rs, rowNum) -> {
            // Extract data from the result set and create CommentaireDTO objects
            CommentaireDto commentaireDTO = new CommentaireDto();
            commentaireDTO.setId(rs.getLong("id"));
            commentaireDTO.setMessage(rs.getString("message"));
            commentaireDTO.setNote(rs.getBigDecimal("note"));
            Long idProprietaire = rs.getLong("proprietaire_id");

            Optional<Personne> optionalProprietaire = personneRepo.findById(idProprietaire);
            if (optionalProprietaire.isPresent()) {
                PersonneDto proprietaireDTO = convertToPersonneDTO(optionalProprietaire.get());
                commentaireDTO.setProprietaire(proprietaireDTO);
            } else {
                System.out.println("Le propriétaire du commentaire n'existe pas");
                commentaireDTO.setProprietaire(null); // Set it to null or handle it accordingly
            }

            return commentaireDTO;
        });
    }




    public static RecetteDto convertToRecetteDto(Recette recette) {
        RecetteDto recetteDto = new RecetteDto();

        recetteDto.setId(recette.getId());
        recetteDto.setNom(recette.getNom());
        recetteDto.setDescription(recette.getDescription());
        recetteDto.setOrigine(recette.getOrigine());
        recetteDto.setDureePreparation(recette.getDureePreparation());
        recetteDto.setDureeCuisson(recette.getDureeCuisson());
        recetteDto.setNbrPersonnes(recette.getNbrPersonnes());
        recetteDto.setVisibilitee(recette.getVisibilitee());
        recetteDto.setStatut(recette.getStatut());
        recetteDto.setDateSoumission(recette.getDateSoumission());
        recetteDto.setDatePublication(recette.getDatePublication());
        if(recette.getUtilisateurCreateur() != null) {
            recetteDto.setUtilisateurCreateur(PersonneImpl.convertToPersonneDTO(recette.getUtilisateurCreateur()));
        }
        if(recette.getUtilisateurApprobateur() != null) {
            recetteDto.setUtilisateurApprobateur(PersonneImpl.convertToPersonneDTO(recette.getUtilisateurApprobateur()));
        }
        if(recette.getSignalants() != null) {
            recetteDto.setSignalants(PersonneImpl.convertToPersonneDTOList(recette.getSignalants()));
        }
        if(recette.getMedias() != null) {
            recetteDto.setMedias(MediaImpl.convertToMediaDTOList(recette.getMedias()));
        }
        if(recette.getBesoins() != null) {
            recetteDto.setBesoins(BesoinImpl.convertToBesoinDTOList(recette.getBesoins()));
        }
        if(recette.getMotCles() != null) {
            recetteDto.setMotCles(MotCleImpl.convertToMotCleDtoList(recette.getMotCles()));
        }
        if(recette.getIngredients() != null) {
            recetteDto.setIngredients(IngredientImpl.convertToIngredientDtoList(recette.getIngredients()));
        }
        if(recette.getEtapes() != null) {
            recetteDto.setEtapes(EtapeImpl.convertToEtapeDtoList(recette.getEtapes()));
        }
        if(recette.getCategories() != null) {
            recetteDto.setCategories(CategorieImpl.convertToCategorieDtoList(recette.getCategories()));
        }
        if(recette.getTypeRelation() != null) {
            recetteDto.setTypeRelation(TypeRelationImpl.convertToTypeRelationDto(recette.getTypeRelation()));
        }
        if(recette.getTypeRel() != null) {
            recetteDto.setTypeRel(TypeRelationImpl.convertToTypeRelationDto(recette.getTypeRel()));
        }
        /*if(recette.getRecettes() != null) {
            recetteDto.setRecettes(convertToRecetteDtoList(recette.getRecettes()));
        }*/
        if (recette.getRecettes() != null) {
            List<RecetteRefDto> recetteRefDtos = new ArrayList<>();
            for (Recette recette1 : recette.getRecettes()) {
                RecetteRefDto recetteRefDto = new RecetteRefDto();
                recetteRefDto.setId(recette1.getId());
                recetteRefDto.setNom(recette1.getNom());
                recetteRefDto.setDescription(recette1.getDescription());
                recetteRefDto.setStatut(recette1.getStatut());
                recetteRefDto.setOrigine(recette1.getOrigine());
                recetteRefDto.setDatePublication(recette1.getDatePublication());
                recetteRefDto.setDateSoumission(recette1.getDateSoumission());
                recetteRefDto.setDureeCuisson(recette1.getDureeCuisson());
                recetteRefDto.setDureePreparation(recette1.getDureePreparation());
                recetteRefDto.setNbrPersonnes(recette1.getNbrPersonnes());
                recetteRefDto.setVisibilitee(recette1.getVisibilitee());
                recetteRefDtos.add(recetteRefDto);
            }
            recetteDto.setRecettesRef(recetteRefDtos);
        }

        if(recette.getQuantites() != null) {
            recetteDto.setQuantites(QuantiteImpl.convertToQuantiteDtoList(recette.getQuantites()));
        }
        return recetteDto;
    }

    public static Recette convertToRecette(RecetteDto recetteDto) {
        Recette recette = new Recette();

        recette.setNom(recetteDto.getNom());
        recette.setDescription(recetteDto.getDescription());
        recette.setOrigine(recetteDto.getOrigine());
        recette.setDureePreparation(recetteDto.getDureePreparation());
        recette.setDureeCuisson(recetteDto.getDureeCuisson());
        recette.setNbrPersonnes(recetteDto.getNbrPersonnes());
        recette.setVisibilitee(recetteDto.getVisibilitee());
        recette.setStatut(recetteDto.getStatut());
        recette.setDateSoumission(LocalDate.now());
        recette.setDatePublication(recetteDto.getDatePublication());
        if(recetteDto.getUtilisateurCreateur() != null) {
            recette.setUtilisateurCreateur(convertToPersonne(recetteDto.getUtilisateurCreateur()));
        }
        if(recetteDto.getUtilisateurApprobateur() != null) {
            recette.setUtilisateurApprobateur(convertToPersonne(recetteDto.getUtilisateurApprobateur()));
        }
        if(recetteDto.getSignalants() != null) {
            recette.setSignalants(PersonneImpl.convertToPersonneList(recetteDto.getSignalants()));
        }
        if(recetteDto.getMedias() != null) {
            recette.setMedias(convertToMediaList(recetteDto.getMedias()));
        }
        if(recetteDto.getBesoins() != null) {
            recette.setBesoins(convertToBesoinList(recetteDto.getBesoins()));
        }
        if(recetteDto.getMotCles() != null) {
            recette.setMotCles(convertToMotCleList(recetteDto.getMotCles()));
        }
        if(recetteDto.getIngredients() != null) {
            recette.setIngredients(convertToIngredientList(recetteDto.getIngredients()));
        }
        if(recetteDto.getEtapes() != null) {
            recette.setEtapes(convertToEtapeList(recetteDto.getEtapes()));
        }
        if(recetteDto.getCategories() != null) {
            recette.setCategories(convertToCategorieList(recetteDto.getCategories()));
        }
        if(recetteDto.getTypeRelation() != null) {
            recette.setTypeRelation(TypeRelationImpl.convertToTypeRelation(recetteDto.getTypeRelation()));
        }
        if(recetteDto.getTypeRel() != null) {
            recette.setTypeRel(TypeRelationImpl.convertToTypeRelation(recetteDto.getTypeRel()));
        }
        /*if(recetteDto.getRecettes() != null) {
            recette.setRecettes(convertToRecetteList(recetteDto.getRecettes()));
        }*/
        if (recetteDto.getRecettesRef() != null){
            List<Recette> recettesRef = new ArrayList<>();
            for (RecetteRefDto recetteRefDto : recetteDto.getRecettesRef()){
                Optional<Recette> optionalRecette2 = recetteRepoStat.findById(recetteRefDto.getId());
                if(optionalRecette2.isPresent()) {
                    Recette recetteAdd = optionalRecette2.get();
                    recettesRef.add(recetteAdd);
                }
            }
            recette.setRecettes(recettesRef);
        }
        if(recetteDto.getQuantites() != null) {
            recette.setQuantites(convertToQuantiteList(recetteDto.getQuantites()));
        }
        return recette;
    }

    public static List<RecetteDto> convertToRecetteDtoList(List<Recette> recettes) {
        List<RecetteDto> recetteDtos = new ArrayList<>();
        for (Recette recette : recettes) {
            recetteDtos.add(convertToRecetteDto(recette));
        }

        return recetteDtos;
    }

    public static List<Recette> convertToRecetteList(List<RecetteDto> recetteDtos) {
        List<Recette> recettes = new ArrayList<>();
        if (recetteDtos != null) {
            for (RecetteDto recetteDto : recetteDtos) {
                recettes.add(convertToRecette(recetteDto));
            }
        }

        return recettes;
    }
}